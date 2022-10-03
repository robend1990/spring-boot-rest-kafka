package com.faceit.assessment.user.domain;

import com.faceit.assessment.user.dto.UserSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

class InMemoryUserRepository implements UserRepository {

    private HashMap<UUID, User> db = new HashMap<>();

    @Override
    public User save(User user) {
        return db.put(user.getId(), user);
    }

    @Override
    public void deleteById(UUID id) {
        db.remove(id);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return db.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(db.values()), pageable, db.size());
    }

    @Override
    public Page<User> findByCriteria(UserSearchRequest userSearchRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User findOneOrThrow(UUID id) {
        return UserRepository.super.findOneOrThrow(id);
    }
}