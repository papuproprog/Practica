package com.mishoo.dates.repositories;

import com.mishoo.dates.models.Date;
import com.mishoo.dates.schemas.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DateRepository extends JpaRepository<Date, Long> {

    boolean existsByBarberAndDateTimeAndStatusNot(Long barber, LocalDateTime dateTime, DataStatus status);

    boolean existsByBarberAndDateTimeAndStatusNotAndIdNot(Long barber, LocalDateTime dateTime, DataStatus status, Long id);

    boolean existsByClientAndDateTimeAndStatusNot(Long client, LocalDateTime dateTime, DataStatus status);

    boolean existsByClientAndDateTimeAndStatusNotAndIdNot(Long client, LocalDateTime dateTime, DataStatus status, Long id);

    List<Date> findByBarberAndStatusNot(Long barber, DataStatus status);

    List<Date> findByBarberAndStatusNotAndIdNot(Long barber, DataStatus status, Long id);

}
