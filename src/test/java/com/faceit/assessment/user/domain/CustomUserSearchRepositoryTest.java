package com.faceit.assessment.user.domain;

import com.faceit.assessment.user.dto.UserSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@DataJpaTest
public class CustomUserSearchRepositoryTest {

    @Autowired
    private UserRepository customUserRepository;

    @BeforeEach
    public void fillDb() {
        customUserRepository.save(robert());
        customUserRepository.save(jennifer());
        customUserRepository.save(john());
    }

    @Test
    public void shouldReturnThreeOneSizedPages() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setSize(1);
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getTotalPages(), is(3));
        assertThat(result.getTotalElements(), is(3l));
        assertThat(result.getSize(), is(1));
    }

    @Test
    public void shouldReturnAllUsersOnOnePage() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getTotalPages(), is(1));
        assertThat(result.getTotalElements(), is(3l));
    }

    @Test
    public void shouldFindUserByFirstName() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setFirst_name("Robert");
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getNumberOfElements(), is(1));
    }

    @Test
    public void shouldFindUserByFirstNameAndLastName() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setFirst_name("Robert");
        searchRequest.setLast_name("Drewniak");
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getNumberOfElements(), is(1));
    }


    @Test
    public void shouldFindUserByFirstNameAndLastNameAndCountry() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setFirst_name("Robert");
        searchRequest.setLast_name("Drewniak");
        searchRequest.setCountry("PL");
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getNumberOfElements(), is(1));
    }

    @Test
    public void shouldFindUserByFirstNameAndLastNameAndCountryAndEmail() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setFirst_name("Robert");
        searchRequest.setLast_name("Drewniak");
        searchRequest.setCountry("PL");
        searchRequest.setEmail("rd@gmail.com");
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getNumberOfElements(), is(1));
    }

    @Test
    public void shouldFindUsersByCountry() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setCountry("UK");
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getNumberOfElements(), is(2));
    }

    @Test
    public void shouldFindUsersByEmail() {
        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setEmail("rd@gmail.com");
        Page<User> result = customUserRepository.findByCriteria(searchRequest);
        assertThat(result.getNumberOfElements(), is(1));
    }


    private User robert() {
        return User.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .country("PL")
                .lastName("Drewniak")
                .firstName("Robert")
                .nickname("rd")
                .email("rd@gmail.com")
                .build();
    }

    private User jennifer() {
        return User.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .country("UK")
                .lastName("Aniston")
                .firstName("Jennifer")
                .nickname("aj")
                .email("aj@gmail.com")
                .build();
    }

    private User john() {
        return User.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .country("UK")
                .lastName("John")
                .firstName("Doe")
                .nickname("jd")
                .email("jd@gmail.com")
                .build();
    }
}
