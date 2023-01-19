package io.tashtabash.charging.repository;

import io.tashtabash.charging.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CompanyRepository extends JpaRepository<Company, Long> {

}
