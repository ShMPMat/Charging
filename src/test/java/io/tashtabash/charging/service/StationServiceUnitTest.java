package io.tashtabash.charging.service;

import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.entity.Station;
import io.tashtabash.charging.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@SpringBootTest
class StationServiceUnitTest {
    @Mock
    StationRepository stationRepository;

    @Mock
    CompanyService companyService;

    @InjectMocks
    StationService stationService;

    @Test
    void saveStation() {
        var company = new Company(5, "Test Name", null);
        var expectedStation = new Station("SName", 0.0, 1.1, company);
        when(stationRepository.save(expectedStation))
                .thenReturn(expectedStation);
        when(companyService.getCompany(company.getId()))
                .thenReturn(company);

        var savedStation = stationService.saveStation(
                expectedStation.getName(),
                expectedStation.getLatitude(),
                expectedStation.getLongitude(),
                expectedStation.getCompany().getId()
        );

        assertEquals(
                expectedStation,
                savedStation
        );
    }

    @Test
    void saveServiceThrowsUnprocessableExceptionOnNonExistentCompany() {
        var company = new Company(5, "Parent", null);
        var expectedStation = new Station("SName", 0.0, 1.1, company);
        when(companyService.getCompany(company.getId()))
                .thenThrow(new NoCompanyFoundException(company.getId()));

        assertThrows(
                UnprocessableStationException.class,
                () -> stationService.saveStation(
                        expectedStation.getName(),
                        expectedStation.getLatitude(),
                        expectedStation.getLongitude(),
                        expectedStation.getCompany().getId()
                )
        );
    }

    @Test
    void getStation() {
        var company = new Company(5, "Parent", null);
        var expectedStation = new Station(1, "SName", 0.0, 1.1, company);
        when(stationRepository.findById(expectedStation.getId()))
                .thenReturn(Optional.of(expectedStation));

        Station foundStation = stationService.getStation(expectedStation.getId());

        assertEquals(
                expectedStation,
                foundStation
        );
    }

    @Test
    void getStationThrowsNotFoundExceptionOnNoStation() {
        assertThrows(
                NoStationFoundException.class,
                () -> stationService.getStation(1)
        );
    }

    @Test
    void updateStation() {
        var company = new Company(5, "Parent", null);
        var newCompany = new Company(2, "New Parent", null);
        var station = new Station(1, "SName", 0.0, 1.1, company);
        var newStation = new Station(1, "SName New", 0.1, -10.1, newCompany);
        when(stationRepository.findById(station.getId()))
                .thenReturn(Optional.of(station));
        when(companyService.getCompany(newCompany.getId()))
                .thenReturn(newCompany);
        when(stationRepository.save(newStation))
                .thenReturn(newStation);

        Station updatedStation = stationService.updateStation(newStation);

        verify(stationRepository, times(1))
                .save(newStation);
        assertEquals(newStation, updatedStation);
    }

    @Test
    void updateCompanyThrowsUnprocessableExceptionOnUnknownParent() {
        var company = new Company(5, "Parent", null);
        var nonExistentCompany = new Company(2, "New Parent", null);
        var station = new Station(1, "SName", 0.0, 1.1, company);
        var newStation = new Station(1, "SName New", 0.1, -10.1, nonExistentCompany);
        when(stationRepository.findById(station.getId()))
                .thenReturn(Optional.of(station));
        when(companyService.getCompany(nonExistentCompany.getId()))
                .thenThrow(new NoCompanyFoundException(nonExistentCompany.getId()));
        when(stationRepository.save(newStation))
                .thenReturn(newStation);

        assertThrows(
                UnprocessableStationException.class,
                () -> stationService.updateStation(newStation)
        );
    }

    @Test
    void updateStationThrowsNotFoundExceptionOnAbsentId() {
        var company = new Company(1, "Parent", null);

        assertThrows(
                NoStationFoundException.class,
                () -> stationService.updateStation(new Station(1, "Name", 0.0, 0.0, company))
        );
    }

    @Test
    void deleteStation() {
        var company = new Company(5, "Parent", null);
        var station = new Station(1, "SName", 0.0, 1.1, company);
        doNothing()
                .when(stationRepository)
                .deleteById(station.getId());
        when(stationRepository.findById(station.getId()))
                .thenReturn(Optional.of(station));

        stationService.deleteStation(station.getId());

        verify(stationRepository, times(1))
                .deleteById(station.getId());
    }

    @Test
    void deleteStationThrowsNotFoundExceptionOnAbsentId() {
        long id = 1;
        when(stationRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                NoStationFoundException.class,
                () -> stationService.deleteStation(id)
        );
    }

    @Test
    @Transactional
    void searchInRadiusOrderByDistance() {
        var company = new Company(1, "Test Name", null);
        var expectedStations = List.of(
                new Station(1, "SName", 1.0, 0.0, company),
                new Station(2, "SName", 1.0, 1.0, company)
        );
        when(stationRepository.searchInRadiusOrderByDistance(0.0, 0.0, 200.0))
                .thenReturn(expectedStations);

        var foundStations = stationService.searchInRadiusOrderByDistance(0.0, 0.0, 200.0);

        verify(stationRepository, times(1))
                .searchInRadiusOrderByDistance(0.0, 0.0, 200);
        assertThat(foundStations).containsExactlyElementsOf(expectedStations);
    }

    @Test
    @Transactional
    void searchOwnedStations() {
        var company = new Company(1, "Test Name", null);
        var expectedStations = List.of(
                new Station(1, "SName", 1.0, 0.0, company),
                new Station(2, "SName", 1.0, 1.0, company)
        );
        when(companyService.getCompany(company.getId()))
                .thenReturn(company);
        when(stationRepository.searchByCompany(company.getId()))
                .thenReturn(expectedStations);

        List<Station> foundStations = stationService.searchByCompany(company.getId());

        verify(stationRepository, times(1))
                .searchByCompany(company.getId());
        assertThat(foundStations).containsExactlyElementsOf(expectedStations);
    }

    @Test
    @Transactional
    void searchOwnedStationsThrowsNotFoundExceptionOnNonExistentCompany() {
        var company = new Company(1, "Test Name", null);
        when(companyService.getCompany(company.getId()))
                .thenThrow(new NoCompanyFoundException(company.getId()));

        assertThrows(
                NoCompanyFoundException.class,
                () -> stationService.searchByCompany(company.getId())
        );
    }
}