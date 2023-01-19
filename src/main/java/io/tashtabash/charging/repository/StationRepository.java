package io.tashtabash.charging.repository;

import io.tashtabash.charging.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface StationRepository extends JpaRepository<Station, Long> {
    @Query(
            value = "SELECT * FROM Station " +
                    "WHERE point(latitude, longitude) <@> point(:latitude, :longitude) <= :radiusKm " +
                    "ORDER BY point(latitude, longitude) <@> point(:latitude, :longitude)",
            nativeQuery = true
    )
    List<Station> searchInRadiusOrderByDistance(double latitude, double longitude, double radiusKm);

    @Query(
            value = "SELECT * FROM Station s WHERE company_id in (" +
                    "   WITH RECURSIVE company_and_children_ids as (" +
                    "      SELECT * FROM Company WHERE id = :companyId" +
                    "      UNION" +
                    "      SELECT child.* FROM Company child " +
                    "         INNER JOIN company_and_children_ids parent ON child.parent_company_id = parent.id" +
                    "   ) SELECT id FROM company_and_children_ids" +
                    ")",
            nativeQuery = true
    )
    List<Station> searchByCompany(long companyId);
}
