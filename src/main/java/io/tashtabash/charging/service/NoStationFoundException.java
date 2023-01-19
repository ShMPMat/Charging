package io.tashtabash.charging.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class NoStationFoundException extends ResponseStatusException {
    public NoStationFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "No Station with id " + id + " found");
    }
}
