package com.tyrdanov.auth_service.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.tyrdanov.auth_service.dto.CreateTransactionDto;
import com.tyrdanov.auth_service.dto.TransactionDto;
import com.tyrdanov.auth_service.dto.UpdateTransactionDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TransactionServiceClient {

        private final WebClient.Builder webClient;

        public TransactionDto getTransactionDto(CreateTransactionDto dto) {
                return webClient
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

        public List<TransactionDto> getTransactionsByUserId(Long senderId) {
                return webClient
                                .baseUrl("http://TRANSACTION-SERVICE")
                                .build()
                                .get()
                                .uri("/api/transaction/user/{senderId}", senderId)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<List<TransactionDto>>() {
                                })
                                .block();
        }

        public void updateTransaction(UpdateTransactionDto dto) {
                webClient
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
