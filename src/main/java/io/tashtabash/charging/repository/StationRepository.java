package io.tashtabash.charging.repository;

import io.tashtabash.charging.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StationRepository extends JpaRepository<Station, Long> {

}
