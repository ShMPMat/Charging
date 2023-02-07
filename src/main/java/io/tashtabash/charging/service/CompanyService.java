package io.tashtabash.charging.service;


import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company saveCompany(String name, Long parentCompanyId) {
        try {
            var parentCompany = parentCompanyId == null ? null : getCompany(parentCompanyId);
            var company = new Company(name, parentCompany);

            return companyRepository.save(company);
        } catch (NoCompanyFoundException e) {
            throw new UnprocessableCompanyException("Parent company with id " + parentCompanyId + " doesn't exist");
        }
    }

    public List<Company> getCompanies() {
        return companyRepository.findAll();
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

        try {
            if (company.getParentCompany() != null) {
                if (company.getParentCompany().getId() == company.getId()) {
                    throw new IncorrectCompanyFormatException("Company cannot be its own parent");
                }

                getCompany(company.getParentCompany().getId());
            }

            return companyRepository.save(company);
        } catch (NoCompanyFoundException e) {
            long parentCompanyId = company.getParentCompany().getId();

            throw new UnprocessableCompanyException("Parent company with id " + parentCompanyId + " doesn't exist");
        }
    }

    @Transactional
    public void deleteCompany(long id) {
        getCompany(id);

        companyRepository.deleteById(id);
    }
}
