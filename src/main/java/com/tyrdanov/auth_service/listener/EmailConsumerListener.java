package com.tyrdanov.auth_service.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.tyrdanov.auth_service.dto.ConfirmEmailResponse;
import com.tyrdanov.auth_service.exception.ResourceNotFoundException;
import com.tyrdanov.auth_service.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailConsumerListener {

    private final UserRepository userRepository;

    @Transactional
    @KafkaListener(topics = "confirm-email-response-topic", groupId = "confirm-email")
    public void consume(ConfirmEmailResponse response) {
        final var userId = response.getUserId();
        final var isConfirmed = response.getIsConfirmed();
        final var user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

       user.setIsConfirmed(isConfirmed);
    }

}
