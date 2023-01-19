package io.tashtabash.charging.controller;


import io.tashtabash.charging.entity.Station;
import io.tashtabash.charging.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/station")
public class StationController {
    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping(value="")
    public ResponseEntity<Station> saveStation(@RequestBody SaveStationDto data) {
        if (data.name().strip().equals("")) {
            return ResponseEntity.badRequest()
                    .build();
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

    @GetMapping(value="/{id}")
    public ResponseEntity<Station> getStation(@PathVariable long id) {
        Station station = stationService.getStation(id);

        return ResponseEntity.ok(station);
    }

    @PutMapping("")
    public ResponseEntity<Station> updateStation(@RequestBody Station station) {
        if (station.getName().strip().equals("")) {
            return ResponseEntity.badRequest()
                    .build();
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
