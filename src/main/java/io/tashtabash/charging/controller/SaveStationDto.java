package io.tashtabash.charging.controller;


public record SaveStationDto(String name, double latitude, double longitude, long companyId) {}
