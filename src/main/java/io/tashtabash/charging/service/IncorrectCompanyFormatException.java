package io.tashtabash.charging.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class IncorrectCompanyFormatException extends ResponseStatusException {
    public IncorrectCompanyFormatException(String msg) {
        super(HttpStatus.BAD_REQUEST, "Incorrect Company format: " + msg);
    }
}
