package io.tashtabash.charging.controller;


import io.tashtabash.charging.entity.Station;
import io.tashtabash.charging.service.IncorrectStationFormatException;
import io.tashtabash.charging.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/station")
public class StationController {
    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    private void checkStationData(String name, double latitude, double longitude) {
        if (name.strip().equals("")) {
            throw new IncorrectStationFormatException("Station name must not be blank");
        }

        checkCoordinates(latitude, longitude);
    }

    private void checkCoordinates(double latitude, double longitude) {
        if (latitude < -90 || 90 < latitude) {
            throw new IncorrectStationFormatException("Station latitude must be in range between -90 and 90");
        }
        if (longitude < -180 || 180 < longitude) {
            throw new IncorrectStationFormatException("Station longitude must be in range between -180 and 180");
        }
    }

    @PostMapping("")
    public ResponseEntity<Station> saveStation(@RequestBody SaveStationDto data) {
        checkStationData(data.name(), data.latitude(), data.longitude());

        Station newStation = stationService.saveStation(
                data.name(),
                data.latitude(),
                data.longitude(),
                data.companyId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newStation);
    }

    @GetMapping("")
    public ResponseEntity<List<Station>> searchStations(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radiusKm
    ) {
        checkCoordinates(latitude, longitude);
        if (radiusKm < 0) {
            throw new IncorrectStationFormatException("Radius must be positive");
        }

        List<Station> stations = stationService.searchInRadiusOrderByDistance(latitude, longitude, radiusKm);

        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStation(@PathVariable long id) {
        Station station = stationService.getStation(id);

        return ResponseEntity.ok(station);
    }

    @PutMapping("")
    public ResponseEntity<Station> updateStation(@RequestBody Station station) {
        checkStationData(station.getName(), station.getLatitude(), station.getLongitude());

        Station updatedStation = stationService.updateStation(station);

        return ResponseEntity.ok(updatedStation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStation(@PathVariable long id) {
        stationService.deleteStation(id);

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}
