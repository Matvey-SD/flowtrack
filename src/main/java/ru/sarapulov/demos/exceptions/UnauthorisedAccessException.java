package ru.sarapulov.demos.exceptions;

public class UnauthorisedAccessException extends RuntimeException {

    @Override
    public String getMessage() {
        return "User doesn't have permissions to complete request";
    }

}
