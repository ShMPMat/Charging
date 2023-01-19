package io.tashtabash.charging.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class NoCompanyFoundException extends ResponseStatusException {
    public NoCompanyFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "No Company with id " + id + " found");
    }
}
