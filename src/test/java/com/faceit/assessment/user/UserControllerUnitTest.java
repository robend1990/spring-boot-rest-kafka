package com.faceit.assessment.user;

import com.faceit.assessment.infrastructure.mvc.ExceptionHandlerAdvice;
import com.faceit.assessment.user.domain.UserFacade;
import com.faceit.assessment.user.dto.UserSearchRequest;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {

    @Mock
    private UserFacade userFacade;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private ExceptionHandlerAdvice exceptionHandler;

    @BeforeEach
    public void initRestAssured() {
        RestAssuredMockMvc.standaloneSetup(userController, exceptionHandler);
    }

    @Test
    public void shouldReturnEmptyContent() {
        when(userFacade.findAll(any(UserSearchRequest.class))).thenReturn(Page.empty());
        assertNotNull(userFacade);
        assertNotNull(userController);
        given().when()
                .get("/users")
                .then()
                .log().ifValidationFails()
                .statusCode(OK.value())
                .body("content", hasSize(0));
    }

    // TODO implement test cases for dtoValidation
}
