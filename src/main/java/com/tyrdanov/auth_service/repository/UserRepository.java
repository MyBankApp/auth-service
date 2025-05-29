package com.tyrdanov.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tyrdanov.auth_service.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByConfirmationCode(String confirmationCode);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
