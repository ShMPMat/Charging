package io.tashtabash.charging.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class IncorrectStationFormatException extends ResponseStatusException {
    public IncorrectStationFormatException(String msg) {
        super(HttpStatus.BAD_REQUEST, "Incorrect Station format: " + msg);
    }
}
