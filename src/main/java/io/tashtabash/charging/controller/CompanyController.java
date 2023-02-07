package io.tashtabash.charging.controller;


import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.entity.Station;
import io.tashtabash.charging.service.CompanyService;
import io.tashtabash.charging.service.IncorrectCompanyFormatException;
import io.tashtabash.charging.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/company")
public class CompanyController {
    private final CompanyService companyService;

    private final StationService stationService;

    @Autowired
    public CompanyController(CompanyService companyService, StationService stationService) {
        this.companyService = companyService;
        this.stationService = stationService;
    }

    @PostMapping(value="")
    public ResponseEntity<Company> saveCompany(@RequestBody SaveCompanyDto data) {
        if (data.name().strip().equals("")) {
            throw new IncorrectCompanyFormatException("Company name must not be blank");
        }

        Company newCompany = companyService.saveCompany(data.name(), data.parentCompanyId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newCompany);
    }

    @GetMapping(value="")
    public ResponseEntity<List<Company>> getCompanies() {
        List<Company> companies = companyService.getCompanies();

        return ResponseEntity.ok(companies);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable long id) {
        Company company = companyService.getCompany(id);

        return ResponseEntity.ok(company);
    }

    //TODO add checks for inconsistent parent data?
    @PutMapping("")
    public ResponseEntity<Company> updateCompany(@RequestBody Company company) {
        if (company.getName().strip().equals("")) {
            throw new IncorrectCompanyFormatException("Company name must not be blank");
        }

        Company updatedCompany = companyService.updateCompany(company);

        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping(value="/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable long id) {
        companyService.deleteCompany(id);

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    @GetMapping(value="/{id}/station")
    public ResponseEntity<List<Station>> searchStations(@PathVariable long id) {
        List<Station> stations = stationService.searchByCompany(id);

        return ResponseEntity.ok(stations);
    }
}
