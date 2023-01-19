package io.tashtabash.charging.service;


import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;


@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company saveCompany(String name, Long parentCompanyId) {
        var parentCompany = parentCompanyId == null ? null : getCompany(parentCompanyId);
        var company = new Company(name, parentCompany);

        return companyRepository.save(company);
    }

    public Company getCompany(long id) {
        Optional<Company> company = companyRepository.findById(id);

        return company.orElseThrow(() ->
                new NoCompanyFoundException(id)
        );
    }

    @Transactional
    public Company updateCompany(Company company) {
        getCompany(company.getId());

        if (company.getParentCompany() != null) {
            getCompany(company.getParentCompany().getId());
        }

        return companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(long id) {
        getCompany(id);

        companyRepository.deleteById(id);
    }
}
