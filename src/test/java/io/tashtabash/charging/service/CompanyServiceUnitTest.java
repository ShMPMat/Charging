package io.tashtabash.charging.service;

import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
class CompanyServiceUnitTest {
    @Mock
    CompanyRepository companyRepository;

    @InjectMocks
    CompanyService companyService;

    @Test
    void saveCompany() {
        var expectedCompany = new Company("Test Name", null);
        when(companyRepository.save(new Company(0, expectedCompany.getName(), null)))
                .thenReturn(expectedCompany);
        when(companyRepository.findById(any()))
                .thenReturn(Optional.empty());

        var company = companyService.saveCompany(expectedCompany.getName(), null);

        assertEquals(
                expectedCompany,
                company
        );
    }

    @Test
    void saveCompanyWithParent() {
        var parentCompany = new Company(5, "Parent", null);
        var expectedCompany = new Company("Test Name", parentCompany);
        when(companyRepository.save(new Company(0, expectedCompany.getName(), parentCompany)))
                .thenReturn(expectedCompany);
        when(companyRepository.findById(parentCompany.getId()))
                .thenReturn(Optional.of(parentCompany));

        Company company = companyService.saveCompany(expectedCompany.getName(), parentCompany.getId());

        assertEquals(
                expectedCompany,
                company
        );
    }

    @Test
    void saveCompanyThrowsUnprocessableExceptionOnNoParent() {
        var parentCompany = new Company(5, "Parent", null);
        var expectedCompany = new Company("Test Name", parentCompany);
        when(companyRepository.save(new Company(0, expectedCompany.getName(), parentCompany)))
                .thenReturn(expectedCompany);
        when(companyRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                UnprocessableCompanyException.class,
                () -> companyService.saveCompany(expectedCompany.getName(), parentCompany.getId())
        );
    }

    @Test
        void getCompanies() {
            var parentCompany = new Company(1, "Test 1", null);
            var expectedCompanies = List.of(
                    new Company(2, "Test 2", null),
                    new Company(3, "Test 3", parentCompany),
                    new Company(4, "Test 4", null)
            );
        when(companyRepository.findAll())
                .thenReturn(expectedCompanies);

        List<Company> companies = companyService.getCompanies();

        assertThat(companies)
                .containsExactlyInAnyOrderElementsOf(expectedCompanies);
    }

    @Test
    void getCompany() {
        var expectedCompany = new Company(1, "Test Name", null);
        when(companyRepository.findById(expectedCompany.getId()))
                .thenReturn(Optional.of(expectedCompany));

        Company company = companyService.getCompany(expectedCompany.getId());

        assertEquals(
                expectedCompany,
                company
        );
    }

    @Test
    void getCompanyWithParent() {
        var parentCompany = new Company(1, "Test Name", null);
        var expectedCompany = new Company(2, "Test Name", parentCompany);
        when(companyRepository.findById(expectedCompany.getId()))
                .thenReturn(Optional.of(expectedCompany));

        Company company = companyService.getCompany(expectedCompany.getId());

        assertEquals(
                expectedCompany,
                company
        );
    }

    @Test
    void getCompanyThrowsNotFoundExceptionOnNoCompany() {
        assertThrows(
                NoCompanyFoundException.class,
                () -> companyService.getCompany(1)
        );
    }

    @Test
    void updateCompany() {
        var company = new Company(1, "Name", null);
        var newCompany = new Company(1, "New Name", null);
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(companyRepository.save(newCompany))
                .thenReturn(newCompany);

        var returnedCompany = companyService.updateCompany(newCompany);

        verify(companyRepository, times(1))
                .save(newCompany);
        assertEquals(newCompany, returnedCompany);
    }

    @Test
    void updateCompanyWithParent() {
        var newParentCompany = new Company(2, "Test Name", null);
        var company = new Company(3, "Name", null);
        var newCompany = new Company(3, "New Name", newParentCompany);
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(companyRepository.findById(newParentCompany.getId()))
                .thenReturn(Optional.of(newParentCompany));
        when(companyRepository.save(newCompany))
                .thenReturn(newCompany);

        var returnedCompany = companyService.updateCompany(newCompany);

        verify(companyRepository, times(1))
                .save(newCompany);
        assertEquals(newCompany, returnedCompany);
    }

    @Test
    void updateCompanyThrowsUnprocessableExceptionOnUnknownParent() {
        var nonExistentCompany = new Company(2, "Test Name", null);
        var company = new Company(1, "Name", nonExistentCompany);
        when(companyRepository.save(company))
                .thenReturn(company);
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
        when(companyRepository.findById(nonExistentCompany.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                UnprocessableCompanyException.class,
                () -> companyService.updateCompany(company)
        );
    }

    @Test
    void updateCompanyThrowsNotFoundExceptionOnAbsentId() {
        assertThrows(
                NoCompanyFoundException.class,
                () -> companyService.updateCompany(new Company(1, "Name", null))
        );
    }

    @Test
    void updateCompanyThrowsIncorrectFormatExceptionOnIdEqualsParentId() {
        var company = new Company(1, "Name", null);
        company.setParentCompany(company);
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));

        assertThrows(
                IncorrectCompanyFormatException.class,
                () -> companyService.updateCompany(company)
        );
        verify(companyRepository, never())
                .save(any());
    }

    @Test
    void deleteCompany() {
        var company = new Company(1, "Name", null);
        doNothing()
                .when(companyRepository)
                .deleteById(company.getId());
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));

        companyService.deleteCompany(company.getId());

        verify(companyRepository, times(1))
                .deleteById(company.getId());
    }

    @Test
    void deleteCompanyThrowsNotFoundExceptionOnAbsentId() {
        assertThrows(
                NoCompanyFoundException.class,
                () -> companyService.deleteCompany(1)
        );
    }
}
