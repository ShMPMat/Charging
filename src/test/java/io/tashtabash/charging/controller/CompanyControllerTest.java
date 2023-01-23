package io.tashtabash.charging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.entity.Station;
import io.tashtabash.charging.service.IncorrectCompanyFormatException;
import io.tashtabash.charging.service.CompanyService;
import io.tashtabash.charging.service.NoCompanyFoundException;
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


@WebMvcTest(CompanyController.class)
class CompanyControllerTest {
    @MockBean
    CompanyService companyService;

    @MockBean
    StationService stationService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @Test
    void saveCompany() throws Exception {
        var resultCompany = new Company(1, "Name 1", null);
        when(companyService.saveCompany(resultCompany.getName(), null))
                .thenReturn(resultCompany);

        var payload = new SaveCompanyDto(resultCompany.getName(), null);
        mockMvc.perform(
                post("/company")
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType("application/json")
                ).andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(resultCompany)));
    }

    @Test
    void saveCompanyWithNoParentInJson() throws Exception {
        var resultCompany = new Company(1, "Name 1", null);
        when(companyService.saveCompany(resultCompany.getName(), null))
                .thenReturn(resultCompany);

        var json = "{\"name\": \"" + resultCompany.getName() + "\"}";
        mockMvc.perform(
                        post("/company")
                                .content(json)
                                .contentType("application/json")
                ).andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(resultCompany)));
    }

    @Test
    void saveCompanyWithParent() throws Exception {
        var parentCompany = new Company(5, "Parent", null);
        var resultCompany = new Company(1, "Name 1", parentCompany);
        when(companyService.saveCompany(resultCompany.getName(), parentCompany.getId()))
                .thenReturn(resultCompany);

        var payload = new SaveCompanyDto(resultCompany.getName(), resultCompany.getParentCompany().getId());
        mockMvc.perform(
                        post("/company")
                                .content(objectMapper.writeValueAsString(payload))
                                .contentType("application/json")
                ).andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(resultCompany)));
    }

    @Test
    void saveCompanyAnswers404OnNoParent() throws Exception {
        var parentCompany = new Company(5, "Parent", null);
        var resultCompany = new Company(1, "Name 1", parentCompany);
        when(companyService.saveCompany(resultCompany.getName(), parentCompany.getId()))
                .thenThrow(new NoCompanyFoundException(parentCompany.getId()));

        var payload = new SaveCompanyDto(resultCompany.getName(), resultCompany.getParentCompany().getId());
        mockMvc.perform(
                        post("/company")
                                .content(objectMapper.writeValueAsString(payload))
                                .contentType("application/json")
        ).andExpect(status().isNotFound());
    }

    @ParameterizedTest()
    @ValueSource(strings = { "", "    ", "\t", "\u205F" })
    void saveCompanyAnswers400WhenWhitespaceName(String emptyName) throws Exception {
        var payload = new SaveCompanyDto(emptyName, null);
        mockMvc.perform(
                post("/company")
                        .content(objectMapper.writeValueAsString(payload))
                        .contentType("application/json")
                        .characterEncoding("UTF-8")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getCompany() throws Exception {
        var company = new Company(1, "N", null);
        when(companyService.getCompany(company.getId()))
                .thenReturn(company);

        mockMvc.perform(get("/company/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(company)));
    }

    @Test
    void getCompanyWithParent() throws Exception {
        var parentCompany = new Company(1, "N", null);
        var company = new Company(1, "N", parentCompany);
        when(companyService.getCompany(company.getId()))
                .thenReturn(company);

        mockMvc.perform(get("/company/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(company)));
    }

    @Test
    void getCompanyAnswers404OnNoCompany() throws Exception {
        when(companyService.getCompany(1))
                .thenThrow(new NoCompanyFoundException(1));

        mockMvc.perform(get("/company/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCompany() throws Exception {
        var company = new Company(1, "N", null);
        when(companyService.updateCompany(company))
                .thenReturn(company);

        mockMvc.perform(
                        put("/company")
                                .content(objectMapper.writeValueAsString(company))
                                .contentType("application/json")
                ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(company)));
    }

    @Test
    void updateCompanyWithParent() throws Exception {
        var newParentCompany = new Company(2, "Test Name", null);
        var company = new Company(3, "Name", newParentCompany);
        when(companyService.updateCompany(company))
                .thenReturn(company);

        mockMvc.perform(
                        put("/company")
                                .content(objectMapper.writeValueAsString(company))
                                .contentType("application/json")
                ).andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(company)));
    }

    @Test
    void updateCompanyAnswers404OnUnknownParent() throws Exception {
        var nonExistentCompany = new Company(2, "Test Name", null);
        var company = new Company(1, "N", nonExistentCompany);
        when(companyService.updateCompany(company))
                .thenThrow(new NoCompanyFoundException(nonExistentCompany.getId()));

        mockMvc.perform(
                        put("/company")
                                .content(objectMapper.writeValueAsString(company))
                                .contentType("application/json")
        ).andExpect(status().isNotFound());
    }

    @Test
    void updateCompanyAnswers404OnAbsentId() throws Exception {
        var company = new Company(1, "N", null);
        when(companyService.updateCompany(any()))
                .thenThrow(new NoCompanyFoundException(company.getId()));

        mockMvc.perform(
                put("/company")
                        .content(objectMapper.writeValueAsString(company))
                        .contentType("application/json")
        ).andExpect(status().isNotFound());
    }

    @Test
    void updateCompanyAnswers400OnIdEqualsParentId() throws Exception {
        var oldCompany = new Company(1, "N", null);
        var incorrectCompany = new Company(1, "N", oldCompany);
        when(companyService.updateCompany(any()))
                .thenThrow(new IncorrectCompanyFormatException("Test Message"));

        mockMvc.perform(
                put("/company")
                        .content(objectMapper.writeValueAsString(incorrectCompany))
                        .contentType("application/json")
        ).andExpect(status().isBadRequest());
    }

    @ParameterizedTest()
    @ValueSource(strings = { "", "    ", "\t", "\u205F" })
    void updateCompanyAnswers400WhenWhitespaceName(String emptyName) throws Exception {
        long id = 1;
        var company = new Company(id, emptyName, null);

        mockMvc.perform(
                put("/company")
                        .content(objectMapper.writeValueAsString(company))
                        .contentType("application/json")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void deleteCompany() throws Exception {
        doNothing()
                .when(companyService).deleteCompany(1);

        mockMvc.perform(delete("/company/1"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[] {}));
    }

    @Test
    void deleteCompanyAnswers404OnAbsentId() throws Exception {
        doThrow(new NoCompanyFoundException(1))
                .when(companyService).deleteCompany(1);

        mockMvc.perform(delete("/company/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchOwnedStations() throws Exception {
        var company = new Company(1, "Test Name", null);
        var expectedStations = List.of(
                new Station(1, "SName", 1.0, 0.0, company),
                new Station(2, "SName", 1.0, 1.0, company)
        );
        when(stationService.searchByCompany(company.getId()))
                .thenReturn(expectedStations);

        mockMvc.perform(get("/company/1/station"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedStations)));
    }
}
