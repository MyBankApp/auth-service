package com.tyrdanov.auth_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tyrdanov.auth_service.dto.ConfirmRegistrationDto;
import com.tyrdanov.auth_service.dto.TransactionDto;
import com.tyrdanov.auth_service.dto.TransferRequest;
import com.tyrdanov.auth_service.dto.UpdateUserDto;
import com.tyrdanov.auth_service.dto.UserDto;
import com.tyrdanov.auth_service.enums.Status;
import com.tyrdanov.auth_service.exception.InsufficientBalanceException;
import com.tyrdanov.auth_service.exception.LimitOperationsException;
import com.tyrdanov.auth_service.exception.ResourceNotFoundException;
import com.tyrdanov.auth_service.exception.TransferFailedException;
import com.tyrdanov.auth_service.factory.TransactionDtoFactory;
import com.tyrdanov.auth_service.mapper.UserMapper;
import com.tyrdanov.auth_service.model.User;
import com.tyrdanov.auth_service.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserMapper mapper;
        private final UserRepository repository;
        private final TransactionServiceClient transactionServiceClient;

        public List<UserDto> getAll() {
                return repository
                                .findAll()
                                .stream()
                                .map(mapper::toDto)
                                .toList();
        }

        public UserDto getById(Long id) {
                final var user = repository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                return mapper.toDto(user);
        }

        public UserDetails getByUsername(String username) {
                final var user = repository
                                .findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                final var password = user.getPassword();

                return new org.springframework.security.core.userdetails.User(
                                username,
                                password,
                                List.of());
        }

        public ConfirmRegistrationDto confirmRegistration(String generatedString) {
                final var optionalUser = repository
                                .findByConfirmationCode(generatedString);

                if (optionalUser.isEmpty()) {
                        return ConfirmRegistrationDto
                                        .builder()
                                        .confirmation(false)
                                        .build();
                }

                final var user = optionalUser.get();

                user.setIsConfirmed(true);
                user.setConfirmationCode(null);
                repository.save(user);

                return ConfirmRegistrationDto
                                .builder()
                                .confirmation(true)
                                .build();
        }

        @Transactional
        public void makeTransfer(TransferRequest request) {
                final var senderId = request.getSenderId();
                final var receiverId = request.getReceiverId();
                final var amount = request.getAmount();
                final var sender = repository
                                .findById(senderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
                final var receiver = repository
                                .findById(receiverId)
                                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
                final var senderBalance = sender.getBalance();
                final var receiverBalance = receiver.getBalance();

                if (senderBalance.compareTo(amount) <= 0) {
                        throw new InsufficientBalanceException("Insufficient balance");
                }

                final var transactionsByUserId = transactionServiceClient.getTransactionsByUserId(senderId);
                final var isConfirmed = sender.getIsConfirmed().booleanValue();
                final var summa = transactionsByUserId
                                .stream()
                                .mapToDouble(dto -> dto.getSenderId().equals(senderId)
                                                ? -dto.getAmount().doubleValue()
                                                : dto.getAmount().doubleValue())
                                .sum();

                if (isConfirmed || (summa >= -5000 && amount.intValue() <= 5000)) {
                        performTransfer(senderBalance, receiverBalance, sender, receiver, request, amount);
                } else {
                        throw new LimitOperationsException("Limit of operations");
                }
        }

        public UserDto update(UpdateUserDto dto) {
                final var id = dto.getId();
                final var user = repository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                mapper.update(dto, user);

                final var savedUser = repository.save(user);

                return mapper.toDto(savedUser);
        }

        public void delete(Long id) {
                repository.deleteById(id);
        }

        private void performTransfer(BigDecimal senderBalance, BigDecimal receiverBalance,
                        User sender, User receiver, TransferRequest request,
                        BigDecimal amount) {
                final var updatedSenderBalance = senderBalance.subtract(amount);
                final var updatedReceiverBalance = receiverBalance.add(amount);
                final var createdTransactionDto = TransactionDtoFactory.buildCreateDto(request);

                TransactionDto transactionDto = null;

                try {
                        transactionDto = transactionServiceClient.getTransactionDto(createdTransactionDto);

                        final var id = transactionDto.getId();
                        final var updateDto = TransactionDtoFactory.buildUpdateDto(id, request, Status.COMPLETED);

                        transactionServiceClient.updateTransaction(updateDto);

                        sender.setBalance(updatedSenderBalance);
                        receiver.setBalance(updatedReceiverBalance);
                } catch (Exception e) {
                        handleFailure(request, transactionDto);
                        throw new TransferFailedException("Transfer failed: " + e.getMessage(), e);
                }
        }

        private void handleFailure(TransferRequest request, TransactionDto transactionDto) {
                if (transactionDto != null) {
                        final var id = transactionDto.getId();
                        final var updateDto = TransactionDtoFactory.buildUpdateDto(id, request, Status.FAILED);
                        try {
                                transactionServiceClient.updateTransaction(updateDto);
                        } catch (Exception ex) {
                                System.err.println("Failed to update transaction status to FAILED: "
                                                + ex.getMessage());
                        }
                }
        }
}
