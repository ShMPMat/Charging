package io.tashtabash.charging.service;


import io.tashtabash.charging.entity.Station;
import io.tashtabash.charging.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;


@Service
public class StationService {
    private final StationRepository stationRepository;

    private final CompanyService companyService;

    @Autowired
    public StationService(StationRepository stationRepository, CompanyService companyService) {
        this.stationRepository = stationRepository;
        this.companyService = companyService;
    }

    @Transactional
    public Station saveStation(String name, double latitude, double longitude, long companyId) {
        var company = companyService.getCompany(companyId);
        var station = new Station(name, latitude, longitude, company);

        return stationRepository.save(station);
    }

    public Station getStation(long id) {
        Optional<Station> station = stationRepository.findById(id);

        return station.orElseThrow(() ->
                new NoStationFoundException(id)
        );
    }

    @Transactional
    public Station updateStation(Station station) {
        Station oldStation = getStation(station.getId());

        if (oldStation.getCompany().getId() != station.getCompany().getId()) {
            companyService.getCompany(station.getCompany().getId());
        }

        return stationRepository.save(station);
    }

    @Transactional
    public void deleteStation(long id) {
        getStation(id);

        stationRepository.deleteById(id);
    }
}
