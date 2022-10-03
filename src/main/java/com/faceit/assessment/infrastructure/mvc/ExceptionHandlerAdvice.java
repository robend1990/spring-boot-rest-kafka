package com.faceit.assessment.infrastructure.mvc;

import com.faceit.assessment.user.exception.UserAlreadyExistsException;
import com.faceit.assessment.user.exception.UserNotFoundException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ErrorMessage> handleUserNotFound(UserNotFoundException e) {
        log.error(e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    ResponseEntity<ErrorMessage> handleUserExists(UserAlreadyExistsException e) {
        log.error(e.getMessage());
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<List<FieldValidationError>> handleDtoValidationError(MethodArgumentNotValidException e) {
        log.error("Request body is invalid!", e);
        List<FieldValidationError> errors = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {

            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.add(new FieldValidationError(fieldName, message));
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @Getter
    class ErrorMessage {
        private String message;

        public ErrorMessage(String message) {
            this.message = message;
        }
    }

    @Getter
    class FieldValidationError extends ErrorMessage {
        private String fieldName;

        public FieldValidationError(String fieldName, String message) {
            super(message);
            this.fieldName = fieldName;
        }
    }
}
