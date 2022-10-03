package com.faceit.assessment.user;


import com.faceit.assessment.user.domain.UserFacade;
import com.faceit.assessment.user.dto.CreateOrUpdateUserDto;
import com.faceit.assessment.user.dto.UserDto;
import com.faceit.assessment.user.dto.UserSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
class UserController {

    private final UserFacade userService;

    @Autowired
    public UserController(UserFacade userService) {
        this.userService = userService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserDto>> findUsers(UserSearchRequest userSearch) {
        return ResponseEntity.ok(userService.findAll(userSearch));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateOrUpdateUserDto userDto) {
        UserDto user = userService.create(userDto);
        URI userLocation = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(userLocation).body(user);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateUser(@PathVariable UUID id, @Valid @RequestBody CreateOrUpdateUserDto userDto) {
        userService.update(id, userDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity removeUser(@PathVariable UUID id) {
        userService.remove(id);
        return ResponseEntity.noContent().build();
    }
}
