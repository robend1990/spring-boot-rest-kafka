package com.faceit.assessment.user.domain;

import com.faceit.assessment.user.dto.CreateOrUpdateUserDto;
import com.faceit.assessment.user.dto.UserDto;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="users")
class User {

    @Id
    @Type(type="org.hibernate.type.UUIDCharType")
    private UUID id;

    @Version
    private Long version;

    private String password;
    private String firstName;
    private String lastName;
    private String nickname;

    @Column(unique=true)
    private String email;
    private String country;
    private Instant createdAt;
    private Instant updatedAt;

    void update(CreateOrUpdateUserDto userDto) {
        password = userDto.getPassword();
        firstName = userDto.getFirstName();
        lastName = userDto.getLastName();
        nickname = userDto.getNickname();
        email = userDto.getEmail();
        country = userDto.getCountry();
        updatedAt = Instant.now();
    }

    UserDto toDto() {
        return UserDto.builder()
                .id(id.toString())
                .firstName(firstName)
                .lastName(lastName)
                .nickname(nickname)
                .country(country)
                .email(email)
                .build();
    }
}
