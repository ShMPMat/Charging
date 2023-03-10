package io.tashtabash.charging.repository;

import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.entity.Station;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CompanyRepositoryTest {
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    private void insertCompany(Company company) {
        Query query = company.getParentCompany() == null
                ? entityManager.createNativeQuery("INSERT INTO Company values(?, ?, null)")
                : entityManager.createNativeQuery("INSERT INTO Company values(?, ?, ?)");

        query.setParameter(1, company.getId())
                .setParameter(2, company.getName());

        if (company.getParentCompany() != null) {
            query.setParameter(3, company.getParentCompany().getId());
        }

        query.executeUpdate();
    }

    private void insertStation(Station station) {
        entityManager.createNativeQuery("INSERT INTO Station values(?, ?, ?, ?, ?)")
                .setParameter(1, station.getId())
                .setParameter(2, station.getName())
                .setParameter(3, station.getLatitude())
                .setParameter(4, station.getLongitude())
                .setParameter(5, station.getCompany().getId())
                .executeUpdate();
    }

    @Test
    void saveCompany() {
        var expectedCompany = new Company("Test Name", null);

        companyRepository.save(expectedCompany);

        List<Company> companies = entityManager
                .createQuery("SELECT c from Company c", Company.class)
                .getResultList();
        assertEquals(1, companies.size());
        assertEquals(expectedCompany.getName(), companies.get(0).getName());
        assertEquals(expectedCompany.getParentCompany(), companies.get(0).getParentCompany());
        assertNotEquals(0, companies.get(0).getId());
    }

    @Test
    @Transactional
    void saveCompanyWithParent() {
        var parentCompany = new Company(5, "Parent", null);
        insertCompany(parentCompany);
        var expectedCompany = new Company("Test", parentCompany);

        companyRepository.save(expectedCompany);

        var query = "SELECT c from Company c WHERE c.id <> 5";
        List<Company> companies = entityManager
                .createQuery(query, Company.class)
                .getResultList();
        assertEquals(1, companies.size());
        assertEquals(expectedCompany.getName(), companies.get(0).getName());
        assertEquals(expectedCompany.getParentCompany(), companies.get(0).getParentCompany());
        assertNotEquals(0, companies.get(0).getId());
    }

    @Test
    void saveCompanyFailsOnNoParent() {
        var parentCompany = new Company(5, "Parent", null);
        var expectedCompany = new Company("Test", parentCompany);

        assertThrows(
                DataAccessException.class,
                () -> companyRepository.save(expectedCompany)
        );
    }

    @Test
    @Transactional
    void getCompanies() {
        var parentCompany = new Company(1, "Test 1", null);
        var expectedCompanies = List.of(
                new Company(2, "Test 2", null),
                new Company(3, "Test 3", parentCompany),
                new Company(4, "Test 4", null)
        );
        insertCompany(parentCompany);
        for (var company : expectedCompanies) {
            insertCompany(company);
        }

        List<Company> foundCompanies = companyRepository.findAll();

        assertThat(foundCompanies)
                .containsExactlyInAnyOrderElementsOf(foundCompanies);
    }

    @Test
    @Transactional
    void getCompany() {
        var expectedCompany = new Company(1, "Test Name", null);
        insertCompany(expectedCompany);

        Optional<Company> foundCompanyOptional = companyRepository.findById(expectedCompany.getId());

        assertTrue(foundCompanyOptional.isPresent());
        assertEquals(expectedCompany, foundCompanyOptional.get());
    }

    @Test
    @Transactional
    void getCompanyWithParent() {
        var grandparentCompany = new Company(1, "Grandparent", null);
        var parentCompany = new Company(2, "Parent", grandparentCompany);
        var expectedCompany = new Company(3, "Test Name", parentCompany);
        insertCompany(grandparentCompany);
        insertCompany(parentCompany);
        insertCompany(expectedCompany);

        Optional<Company> foundCompanyOptional = companyRepository.findById(expectedCompany.getId());

        assertTrue(foundCompanyOptional.isPresent());
        assertEquals(expectedCompany, foundCompanyOptional.get());
    }

    @Test
    void getCompanyReturnsEmptyOptionalOnNoCompany() {
        Optional<Company> foundCompanyOptional = companyRepository.findById(1L);

        assertTrue(foundCompanyOptional.isEmpty());
    }

    @Test
    @Transactional
    void updateCompany() {
        var expectedCompany = new Company(1, "Test Name", null);
        insertCompany(expectedCompany);
        var newCompany = new Company(1, "New Name", null);

        companyRepository.save(newCompany);

        List<Company> companies = entityManager
                .createQuery("SELECT c from Company c", Company.class)
                .getResultList();
        assertEquals(1, companies.size());
        assertEquals(newCompany, companies.get(0));
    }

    @Test
    @Transactional
    void updateCompanyWithParent() {
        var parentCompany = new Company(1, "Test Name", null);
        var newParentCompany = new Company(2, "Test Name", null);
        var expectedCompany = new Company(3, "Test Name", parentCompany);
        insertCompany(parentCompany);
        insertCompany(newParentCompany);
        insertCompany(expectedCompany);
        var newCompany = new Company(3, "New Name", newParentCompany);

        companyRepository.save(newCompany);

        List<Company> companies = entityManager
                .createQuery("SELECT c from Company c WHERE c.id = 3", Company.class)
                .getResultList();
        assertEquals(1, companies.size());
        assertEquals(newCompany, companies.get(0));
    }

    @Test
    @Transactional
    void updateCompanyThrowsOnUnknownParent() {
        var nonExistentCompany = new Company(2, "Test Name", null);
        var expectedCompany = new Company(1, "Test Name", null);
        insertCompany(expectedCompany);
        var newCompany = new Company(1, "New Name", nonExistentCompany);

        assertThrows(
                DataAccessException.class,
                () -> companyRepository.save(newCompany)
        );
    }

    @Test
    @Transactional
    void deleteCompany() {
        var expectedCompany = new Company(1, "Test Name", null);
        insertCompany(expectedCompany);

        assertDoesNotThrow(() ->
                companyRepository.deleteById(expectedCompany.getId())
        );

        List<Company> companies = entityManager
                .createQuery("SELECT c from Company c", Company.class)
                .getResultList();
        assertEquals(0, companies.size());
    }

    @Test
    void deleteCompanyThrowsOnNoCompany() {
        assertThrows(
                DataAccessException.class,
                () -> companyRepository.deleteById(1L)
        );
    }

    @Test
    @Transactional
    void deleteCompanyCascades() {
        var parentCompany = new Company(5, "Test Name", null);
        var childCompany = new Company(1, "Test Name", parentCompany);
        insertCompany(parentCompany);
        insertCompany(childCompany);

        assertDoesNotThrow(() -> companyRepository.deleteById(parentCompany.getId()));

        List<Company> companies = entityManager
                .createQuery("SELECT c from Company c", Company.class)
                .getResultList();
        assertEquals(0, companies.size());
    }


    @Test
    @Transactional
    void deleteCompanyCascadesToStations() {
        var company = new Company(1, "Test Name", null);
        var expectedStation = new Station(1, "SName", 0.0, 1.1, company);
        insertCompany(company);
        insertStation(expectedStation);

        assertDoesNotThrow(() -> companyRepository.deleteById(company.getId()));
        entityManager.flush();

        List<Station> stations = entityManager
                .createQuery("SELECT s from Station s", Station.class)
                .getResultList();
        assertEquals(0, stations.size());
    }
}
