package com.mishoo.dates.repositories;

import com.mishoo.dates.models.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DateRepository  extends JpaRepository<Date,Long> {
}
