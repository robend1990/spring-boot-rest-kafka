package com.faceit.assessment.user.domain;

import com.faceit.assessment.user.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

interface UserRepository extends Repository<User, UUID>, UserCustomRepository {

    User save(User user);

    void deleteById(UUID id);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    default User findOneOrThrow(UUID id) {
        return findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
