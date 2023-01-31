package io.tashtabash.charging.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class UnprocessableStationException extends ResponseStatusException {
    public UnprocessableStationException(String msg) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Station is unprocessable: " + msg);
    }
}
