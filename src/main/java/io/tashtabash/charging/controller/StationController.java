package io.tashtabash.charging.controller;


import io.tashtabash.charging.entity.Station;
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

    private ResponseEntity<Station> checkStationData(String name, double latitude, double longitude) {
        if (name.strip().equals("")) {
            return ResponseEntity.badRequest()
                    .build();
        }
        if (latitude < -90 || 90 < latitude) {
            return ResponseEntity.badRequest()
                    .build();
        }
        if (longitude < -180 || 180 < longitude) {
            return ResponseEntity.badRequest()
                    .build();
        }

        return null;
    }

    @PostMapping(value="")
    public ResponseEntity<Station> saveStation(@RequestBody SaveStationDto data) {
        ResponseEntity<Station> badRequestResponse = checkStationData(data.name(), data.latitude(), data.longitude());
        if (badRequestResponse != null) {
            return badRequestResponse;
        }

        Station newStation = stationService.saveStation(
                data.name(),
                data.latitude(),
                data.longitude(),
                data.companyId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newStation);
    }

    @GetMapping(value="")
    public ResponseEntity<List<Station>> searchStations(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radiusKm
    ) {
        if (radiusKm < 0) {
            return ResponseEntity.badRequest()
                    .build();
        }

        List<Station> stations = stationService.searchInRadiusOrderByDistance(latitude, longitude, radiusKm);

        return ResponseEntity.ok(stations);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<Station> getStation(@PathVariable long id) {
        Station station = stationService.getStation(id);

        return ResponseEntity.ok(station);
    }

    @PutMapping("")
    public ResponseEntity<Station> updateStation(@RequestBody Station station) {
        ResponseEntity<Station> badRequestResponse = checkStationData(
                station.getName(),
                station.getLatitude(),
                station.getLongitude()
        );
        if (badRequestResponse != null) {
            return badRequestResponse;
        }

        Station updatedStation = stationService.updateStation(station);

        return ResponseEntity.ok(updatedStation);
    }

    @DeleteMapping(value="/{id}")
    public ResponseEntity<?> deleteStation(@PathVariable long id) {
        stationService.deleteStation(id);

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}
