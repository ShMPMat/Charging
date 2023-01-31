package io.tashtabash.charging.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class UnprocessableCompanyException extends ResponseStatusException {
    public UnprocessableCompanyException(String msg) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Company is unprocessable: " + msg);
    }
}
