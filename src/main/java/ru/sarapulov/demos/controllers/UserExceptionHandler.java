package ru.sarapulov.demos.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.sarapulov.demos.exceptions.UnauthorisedAccessException;

@ControllerAdvice
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UnauthorisedAccessException.class)
    private String handleUnauthorisedAccess(Exception ex) {
        log.warn(ex.getMessage());
        return "redirect:/home";
    }

}
