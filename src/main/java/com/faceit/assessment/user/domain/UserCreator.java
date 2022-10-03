package com.faceit.assessment.user.domain;

import com.faceit.assessment.user.dto.CreateOrUpdateUserDto;
import lombok.extern.java.Log;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Log
class UserCreator {

    private final PasswordEncoder passwordEncoder;

    UserCreator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    User from(CreateOrUpdateUserDto userDto) {
        requireNonNull(userDto);
        String passwordHash = getUserPasswordHash(userDto);
        return User.builder()
                .id(UUID.randomUUID())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(passwordHash)
                .country(userDto.getCountry())
                .nickname(userDto.getNickname())
                .createdAt(Instant.now())
                .build();
    }

    String getUserPasswordHash(CreateOrUpdateUserDto userDto) {
        log.info("Generating password hash");
        requireNonNull(userDto);
        return passwordEncoder.encode(userDto.getPassword());
    }
}
