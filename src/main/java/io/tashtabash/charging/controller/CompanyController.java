package io.tashtabash.charging.controller;


import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/company")
public class CompanyController {
    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping(value="")
    public ResponseEntity<Company> saveCompany(@RequestBody SaveCompanyDto data) {
        if (data.name().strip().equals("")) {
            return ResponseEntity.badRequest()
                    .build();
        }

        Company newCompany = companyService.saveCompany(data.name(), data.parentCompanyId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newCompany);
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
            return ResponseEntity.badRequest()
                    .build();
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
}
