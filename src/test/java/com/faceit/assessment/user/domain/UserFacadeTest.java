package com.faceit.assessment.user.domain;

import com.faceit.assessment.infrastructure.DomainEventPublisher;
import com.faceit.assessment.user.dto.CreateOrUpdateUserDto;
import com.faceit.assessment.user.exception.UserAlreadyExistsException;
import com.faceit.assessment.user.dto.UserDto;
import com.faceit.assessment.user.exception.UserNotFoundException;
import com.faceit.assessment.user.event.UserCreatedEvent;
import com.faceit.assessment.user.event.UserEvent;
import com.faceit.assessment.user.event.UserRemovedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserFacadeTest {

    @Mock
    private DomainEventPublisher<UserEvent> domainEventPublisher;

    private UserFacade userFacade;

    @BeforeEach
    public void setUp() {
       userFacade = new UserConfiguration().userFacade(domainEventPublisher);
    }

    @Test
    public void shouldCreateNewUser() {
        CreateOrUpdateUserDto robert = robert("email");
        UserDto userDto = userFacade.create(robert);
        validateUserDto(robert, userDto);

        UserEvent expectedEvent = new UserCreatedEvent(userDto.getId(), userDto.getEmail());
        verify(domainEventPublisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldNotCreateUserIfEmailAlreadyExists() {
        String sharedEmail = "email";
        CreateOrUpdateUserDto robert = robert(sharedEmail);
        CreateOrUpdateUserDto jennifer = jennifer(sharedEmail);
        userFacade.create(robert);
        assertThrows(UserAlreadyExistsException.class, () -> userFacade.create(jennifer));
    }

    @Test
    public void shouldRemoveUser() {
        CreateOrUpdateUserDto robert = robert("email");
        UserDto userDto = userFacade.create(robert);

        userFacade.remove(UUID.fromString(userDto.getId()));

        Page<UserDto> all = userFacade.findAll(Pageable.unpaged());
        assertTrue(all.getSize() == 0);
        UserEvent expectedEvent = new UserRemovedEvent(userDto.getId());
        verify(domainEventPublisher).publishEvent(eq(expectedEvent));
    }

    @Test
    public void shouldThrowExceptionRemovingUserThatDoesNotExist() {
        assertThrows(UserNotFoundException.class, () -> userFacade.remove(UUID.randomUUID()));
    }

    @Test
    public void shouldUpdateUser() {
        CreateOrUpdateUserDto robert = robert("email");
        UUID existingUserId = UUID.fromString(userFacade.create(robert).getId());

        CreateOrUpdateUserDto updateRequest = CreateOrUpdateUserDto.builder()
                .firstName("Andrew")
                .lastName("Golota")
                .nickname("ag")
                .password("passwd")
                .email("newEmail")
                .build();

        UserDto updatedUser = userFacade.update(existingUserId, updateRequest);
        Page<UserDto> all = userFacade.findAll(Pageable.unpaged());
        assertTrue(all.getSize() == 1);
        validateUserDto(updateRequest, updatedUser);
    }

    @Test
    public void shouldThrowExceptionIfUserChangesEmailToTheOneThatIsAlreadyOccupied() {
        String occupiedEmail = "email";
        CreateOrUpdateUserDto userWithOccupiedEmail = robert(occupiedEmail);
        CreateOrUpdateUserDto userToUpdate = jennifer("oldEmail");

        userFacade.create(userWithOccupiedEmail);
        UUID existingUserId = UUID.fromString(userFacade.create(userToUpdate).getId());

        userToUpdate.setEmail(occupiedEmail);

        assertThrows(UserAlreadyExistsException.class, () -> userFacade.update(existingUserId, userToUpdate));
    }

    @Test
    public void shouldFindAllUsers() {
        CreateOrUpdateUserDto robert = robert("email");
        CreateOrUpdateUserDto jennifer = jennifer("newEmail");

        userFacade.create(robert);
        userFacade.create(jennifer);
        Page<UserDto> all = userFacade.findAll(Pageable.unpaged());
        assertTrue(all.getSize() == 2);

        Page<UserDto> justOne = userFacade.findAll(Pageable.ofSize(1));
        assertTrue(justOne.getSize() == 1);
    }

    private CreateOrUpdateUserDto robert(String email) {
        return CreateOrUpdateUserDto.builder()
                .firstName("Robert")
                .lastName("Drewniak")
                .nickname("rd")
                .password("passwd")
                .email(email)
                .build();
    }

    private CreateOrUpdateUserDto jennifer(String email) {
        return CreateOrUpdateUserDto.builder()
                .firstName("Jennifer")
                .lastName("Aniston")
                .nickname("ja")
                .password("passwd")
                .email(email)
                .build();
    }

    private void validateUserDto(CreateOrUpdateUserDto createOrUpdateUserDto, UserDto userDto) {
        assertEquals(createOrUpdateUserDto.getFirstName(), userDto.getFirstName());
        assertEquals(createOrUpdateUserDto.getLastName(), userDto.getLastName());
        assertEquals(createOrUpdateUserDto.getEmail(), userDto.getEmail());
        assertEquals(createOrUpdateUserDto.getCountry(), userDto.getCountry());
        assertEquals(createOrUpdateUserDto.getNickname(), userDto.getNickname());
        assertIsUUID(userDto.getId());
    }

    private void assertIsUUID(String uuidCandidate) {
        UUID.fromString(uuidCandidate);
    }
}