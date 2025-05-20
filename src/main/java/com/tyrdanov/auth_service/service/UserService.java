package com.tyrdanov.auth_service.service;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.tyrdanov.auth_service.dto.CreateTransactionDto;
import com.tyrdanov.auth_service.dto.TransactionDto;
import com.tyrdanov.auth_service.dto.TransferRequest;
import com.tyrdanov.auth_service.dto.UpdateTransactionDto;
import com.tyrdanov.auth_service.dto.UpdateUserDto;
import com.tyrdanov.auth_service.dto.UserDto;
import com.tyrdanov.auth_service.enums.Status;
import com.tyrdanov.auth_service.exception.InsufficientBalanceException;
import com.tyrdanov.auth_service.exception.ResourceNotFoundException;
import com.tyrdanov.auth_service.exception.TransferFailedException;
import com.tyrdanov.auth_service.factory.TransactionDtoFactory;
import com.tyrdanov.auth_service.mapper.UserMapper;
import com.tyrdanov.auth_service.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserMapper mapper;
        private final UserRepository repository;
        private final WebClient.Builder clientBuilder;
        private final BCryptPasswordEncoder bCryptPasswordEncoder;

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

                final var updatedSenderBalance = senderBalance.subtract(amount);
                final var updatedReceiverBalance = receiverBalance.add(amount);
                final var createdTransactionDto = TransactionDtoFactory.buildCreateDto(request);

                TransactionDto transactionDto = null;

                try {
                        transactionDto = getTransactionDto(createdTransactionDto);

                        final var id = transactionDto.getId();
                        final var updateDto = TransactionDtoFactory.buildUpdateDto(id, request, Status.COMPLETED);

                        updateTransaction(updateDto);

                        sender.setBalance(updatedSenderBalance);
                        receiver.setBalance(updatedReceiverBalance);
                } catch (Exception e) {
                        handleFailure(request, transactionDto);
                        throw new TransferFailedException("Transfer failed: " + e.getMessage(), e);
                }
        }

        public UserDto update(UpdateUserDto dto) {
                final var id = dto.getId();
                final var password = dto.getPassword();
                final var encodedPassword = bCryptPasswordEncoder.encode(password);
                final var user = repository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                dto.setPassword(encodedPassword);
                mapper.update(dto, user);

                final var savedUser = repository.save(user);

                return mapper.toDto(savedUser);
        }

        public void delete(Long id) {
                repository.deleteById(id);
        }

        private void handleFailure(TransferRequest request, TransactionDto transactionDto) {
                if (transactionDto != null) {
                        final var id = transactionDto.getId();
                        final var updateDto = TransactionDtoFactory.buildUpdateDto(id, request, Status.FAILED);
                        try {
                                updateTransaction(updateDto);
                        } catch (Exception ex) {
                                System.err.println("Failed to update transaction status to FAILED: "
                                                + ex.getMessage());
                        }
                }
        }

        private TransactionDto getTransactionDto(CreateTransactionDto dto) {
                return clientBuilder
                                .baseUrl("http://TRANSACTION-SERVICE")
                                .build()
                                .post()
                                .uri("/api/transaction")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(dto)
                                .accept(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> response
                                                .bodyToMono(String.class)
                                                .flatMap(errorBody -> Mono.error(
                                                                new RuntimeException("Error in microservice"
                                                                                + errorBody))))
                                .bodyToMono(TransactionDto.class)
                                .block();
        }

        private void updateTransaction(UpdateTransactionDto dto) {
                clientBuilder
                                .baseUrl("http://TRANSACTION-SERVICE")
                                .build()
                                .put()
                                .uri("/api/transaction")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(dto)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> response
                                                .bodyToMono(String.class)
                                                .flatMap(error -> Mono.error(
                                                                new RuntimeException(
                                                                                "Failed to mark transaction as FAILED: "
                                                                                                + error))))
                                .bodyToMono(Void.class)
                                .block();
        }
}
