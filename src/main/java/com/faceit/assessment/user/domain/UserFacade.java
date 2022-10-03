package com.faceit.assessment.user.domain;

import com.faceit.assessment.infrastructure.DomainEventPublisher;
import com.faceit.assessment.user.dto.CreateOrUpdateUserDto;
import com.faceit.assessment.user.exception.UserAlreadyExistsException;
import com.faceit.assessment.user.dto.UserDto;
import com.faceit.assessment.user.dto.UserSearchRequest;
import com.faceit.assessment.user.event.UserCreatedEvent;
import com.faceit.assessment.user.event.UserRemovedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Slf4j
@Transactional
public class UserFacade {

    private final UserRepository userRepository;
    private final UserCreator userCreator;
    private final DomainEventPublisher domainEventPublisher;

    public UserFacade(UserRepository userRepository,
                      UserCreator userCreator, DomainEventPublisher domainEventPublisher) {
        this.userRepository = userRepository;
        this.userCreator = userCreator;
        this.domainEventPublisher = domainEventPublisher;
    }

    public UserDto create(CreateOrUpdateUserDto userDto) {
        log.info("Creating a user");
        requireNonNull(userDto);
        checkIfUserAlreadyExists(userDto);
        User user = userCreator.from(userDto);
        userRepository.save(user);
        domainEventPublisher.publishEvent(new UserCreatedEvent(user.getId().toString(), user.getEmail()));
        log.info("User created");
        return user.toDto();
    }

    public void remove(UUID userId) {
        log.info("Removing user with id: {}", userId);
        requireNonNull(userId);
        userRepository.findOneOrThrow(userId);
        userRepository.deleteById(userId);
        domainEventPublisher.publishEvent(new UserRemovedEvent(userId.toString()));
        log.info("User removed");
    }

    public UserDto update(UUID userId, CreateOrUpdateUserDto userDto) {
        log.info("Updating user with id: {}", userId);
        requireNonNull(userDto);
        User user = userRepository.findOneOrThrow(userId);
        checkIfEmailAlreadyOccupiedByDifferentUser(userId, userDto);
        String hashedPassword = userCreator.getUserPasswordHash(userDto);
        userDto.setPassword(hashedPassword);
        user.update(userDto);
        User updatedUser = userRepository.save(user);
        log.info("User updated");
        return updatedUser.toDto();
    }

    public Page<UserDto> findAll(Pageable pageable) {
        Page<User> allEntities = userRepository.findAll(pageable);
        return allEntities.map(User::toDto);
    }

    public Page<UserDto> findAll(UserSearchRequest searchRequest) {
        Page<User> allEntities = userRepository.findByCriteria(searchRequest);
        return allEntities.map(User::toDto);
    }

    private void checkIfUserAlreadyExists(CreateOrUpdateUserDto userDto) {
        Optional<User> user = userRepository.findByEmail(userDto.getEmail());
        if (user.isPresent()) {
            throw new UserAlreadyExistsException(userDto.getEmail());
        }
    }

    private void checkIfEmailAlreadyOccupiedByDifferentUser(UUID userId, CreateOrUpdateUserDto userDto) {
        Optional<User> user = userRepository.findByEmail(userDto.getEmail());
        if (user.isPresent() && !user.get().getId().equals(userId)) {
            throw new UserAlreadyExistsException(userDto.getEmail());
        }
    }
}
