package com.mishoo.barbers.repositories;

import com.mishoo.barbers.models.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceItem, Long> {
    Optional<ServiceItem> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
