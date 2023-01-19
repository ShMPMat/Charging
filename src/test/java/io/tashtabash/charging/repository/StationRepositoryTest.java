package io.tashtabash.charging.repository;

import io.tashtabash.charging.entity.Company;
import io.tashtabash.charging.entity.Station;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.annotation.DirtiesContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StationRepositoryTest {
    @Autowired
    private StationRepository stationRepository;

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
    @Transactional
    void saveStation() {
        var company = new Company(1, "Test Name", null);
        var expectedStation = new Station("SName", 0.0, 1.1, company);
        insertCompany(company);

        stationRepository.save(expectedStation);

        List<Station> stations = entityManager
                .createQuery("SELECT s from Station s", Station.class)
                .getResultList();
        assertEquals(1, stations.size());
        assertEquals(expectedStation.getName(), stations.get(0).getName());
        assertEquals(expectedStation.getCompany(), stations.get(0).getCompany());
        assertEquals(expectedStation.getLatitude(), stations.get(0).getLatitude());
        assertEquals(expectedStation.getLongitude(), stations.get(0).getLongitude());
        assertNotEquals(0, stations.get(0).getId());
    }

    @Test
    void saveStationFailsOnNonExistentCompany() {
        var company = new Company(5, "Parent", null);
        var expectedStation = new Station("SName", 0.0, 1.1, company);

        assertThrows(
                DataAccessException.class,
                () -> stationRepository.save(expectedStation)
        );
    }

    @Test
    @Transactional
    void getStation() {
        var company = new Company(1, "Test Name", null);
        var expectedStation = new Station(1, "SName", 0.0, 1.1, company);
        insertCompany(company);
        insertStation(expectedStation);

        Optional<Station> foundStationOptional = stationRepository.findById(expectedStation.getId());

        assertTrue(foundStationOptional.isPresent());
        assertEquals(expectedStation, foundStationOptional.get());
    }

    @Test
    void getStationReturnsEmptyOptionalOnNoStation() {
        Optional<Station> foundCompanyOptional = stationRepository.findById(1L);

        assertTrue(foundCompanyOptional.isEmpty());
    }

    @Test
    @Transactional
    void updateStation() {
        var company = new Company(1, "Test Name", null);
        var newCompany = new Company(2, "Test Name 2", company);
        var expectedStation = new Station(1, "SName", 0.0, 1.1, company);
        insertCompany(company);
        insertCompany(newCompany);
        insertStation(expectedStation);
        var newStation = new Station(1, "SName 2", -10.0, 1.101, newCompany);

        stationRepository.save(newStation);

        List<Station> stations = entityManager
                .createQuery("SELECT s from Station s", Station.class)
                .getResultList();
        assertEquals(1, stations.size());
        assertEquals(newStation, stations.get(0));
    }

    @Test
    @Transactional
    void updateStationThrowsOnUnknownCompany() {
        var company = new Company(1, "Test Name", null);
        var nonExistentCompany = new Company(2, "Test Name", null);
        var expectedStation = new Station(1, "SName", 0.0, 1.1, company);
        insertCompany(company);
        insertStation(expectedStation);
        var newStation = new Station(1, "SName 2", -10.0, 1.101, nonExistentCompany);

        assertThrows(
                DataAccessException.class,
                () -> stationRepository.save(newStation)
        );
    }

    @Test
    @Transactional
    void deleteStation() {
        var company = new Company(1, "Test Name", null);
        var expectedStation = new Station(1, "SName", 0.0, 1.1, company);
        insertCompany(company);
        insertStation(expectedStation);

        assertDoesNotThrow(() ->
                stationRepository.deleteById(expectedStation.getId())
        );

        List<Station> stations = entityManager
                .createQuery("SELECT s from Station s", Station.class)
                .getResultList();
        assertEquals(0, stations.size());
    }

    @Test
    void deleteStationThrowsOnNoStation() {
        assertThrows(
                DataAccessException.class,
                () -> stationRepository.deleteById(1L)
        );
    }
}
