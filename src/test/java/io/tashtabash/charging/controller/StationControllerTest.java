package io.tashtabash.charging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.entity.Station;
import io.tashtabash.charging.service.NoCompanyFoundException;
import io.tashtabash.charging.service.NoStationFoundException;
import io.tashtabash.charging.service.StationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(StationController.class)
class StationControllerTest {
    @MockBean
    StationService stationService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @Test
    void saveStation() throws Exception {
        var company = new Company(5, "Test Name", null);
        var station = new Station("SName", 0.0, 1.1, company);
        when(stationService.saveStation(
                station.getName(),
                station.getLatitude(),
                station.getLongitude(),
                station.getCompany().getId())
        ).thenReturn(station);

        var payload = new SaveStationDto(
                station.getName(),
                station.getLatitude(),
                station.getLongitude(),
                station.getCompany().getId()
        );
        mockMvc.perform(
                post("/station")
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType("application/json")
                ).andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(station)));
    }

    @Test
    void saveStationAnswers404OnNoParent() throws Exception {
        var company = new Company(5, "Test Name", null);
        var station = new Station("SName", 0.0, 1.1, company);
        when(stationService.saveStation(
                station.getName(),
                station.getLatitude(),
                station.getLongitude(),
                station.getCompany().getId())
        ).thenThrow(new NoCompanyFoundException(company.getId()));

        var payload = new SaveStationDto(
                station.getName(),
                station.getLatitude(),
                station.getLongitude(),
                station.getCompany().getId()
        );
        mockMvc.perform(
                        post("/station")
                                .content(objectMapper.writeValueAsString(payload))
                                .contentType("application/json")
        ).andExpect(status().isNotFound());
    }

    @ParameterizedTest()
    @ValueSource(strings = { "", "    ", "\t", "\u205F" })
    void saveStationAnswers400WhenWhitespaceName(String emptyName) throws Exception {
        var payload = new SaveStationDto(emptyName, 0.0, 0.0, 1);
        mockMvc.perform(
                post("/station")
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType("application/json")
                        .characterEncoding("UTF-8")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getStation() throws Exception {
        var company = new Company(5, "Test Name", null);
        var station = new Station(2, "SName", 0.0, 1.1, company);
        when(stationService.getStation(station.getId()))
                .thenReturn(station);

        mockMvc.perform(get("/station/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(station)));
    }

    @Test
    void getStationAnswers404OnNoStation() throws Exception {
        when(stationService.getStation(1))
                .thenThrow(new NoStationFoundException(1));

        mockMvc.perform(get("/station/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStationsInRadius() throws Exception {
        var company = new Company(5, "Test Name", null);
        var expectedStations = List.of(
                new Station(1, "SName", 1.0, 0.0, company),
                new Station(2, "SName", 1.0, 1.0, company)
        );
        when(stationService.searchInRadiusOrderByDistance(0.0, 0.0, 200.0))
                .thenReturn(expectedStations);

        mockMvc.perform(get("/station?latitude=0.0&longitude=0.0&radiusKm=200.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedStations)));
    }

    @Test
    void getStationsInRadiusThrow400OnNegativeRadius() throws Exception {
        mockMvc.perform(get("/station?latitude=0.0&longitude=0.0&radiusKm=-200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStation() throws Exception {
        var company = new Company(5, "Test Name", null);
        var station = new Station(2, "SName", 0.0, 1.1, company);
        when(stationService.updateStation(station))
                .thenReturn(station);

        mockMvc.perform(
                        put("/station")
                                .content(objectMapper.writeValueAsString(station))
                                .contentType("application/json")
                ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(station)));
    }

    @Test
    void updateStationAnswers404OnUnknownCompany() throws Exception {
        var nonExistentCompany = new Company(2, "Test Name", null);
        var station = new Station(2, "SName", 0.0, 1.1, nonExistentCompany);
        when(stationService.updateStation(station))
                .thenThrow(new NoCompanyFoundException(nonExistentCompany.getId()));

        mockMvc.perform(
                        put("/station")
                                .content(objectMapper.writeValueAsString(station))
                                .contentType("application/json")
        ).andExpect(status().isNotFound());
    }

    @Test
    void updateStationAnswers404OnAbsentId() throws Exception {
        var company = new Company(5, "Test Name", null);
        var station = new Station(2, "SName", 0.0, 1.1, company);
        when(stationService.updateStation(any()))
                .thenThrow(new NoStationFoundException(station.getId()));

        mockMvc.perform(
                put("/station")
                        .content(objectMapper.writeValueAsString(station))
                        .contentType("application/json")
        ).andExpect(status().isNotFound());
    }

    @ParameterizedTest()
    @ValueSource(strings = { "", "    ", "\t", "\u205F" })
    void updateStationAnswers400WhenWhitespaceName(String emptyName) throws Exception {
        var company = new Company(5, "Test Name", null);
        var station = new Station(2, emptyName, 0.0, 1.1, company);

        mockMvc.perform(
                put("/station")
                        .content(objectMapper.writeValueAsString(station))
                        .contentType("application/json")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void deleteStation() throws Exception {
        doNothing()
                .when(stationService).deleteStation(1);

        mockMvc.perform(delete("/station/1"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[] {}));
    }

    @Test
    void deleteStationAnswers404OnAbsentId() throws Exception {
        doThrow(new NoStationFoundException(1))
                .when(stationService).deleteStation(1);

        mockMvc.perform(delete("/station/1"))
                .andExpect(status().isNotFound());
    }
}
