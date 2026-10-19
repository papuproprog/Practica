package com.mishoo.dates.services;

import com.mishoo.dates.adapters.ClientAdapter;
import com.mishoo.dates.exceptions.DateNotFoundException;
import com.mishoo.dates.models.Date;
import com.mishoo.dates.repositories.DateRepository;
import com.mishoo.dates.schemas.DateRequest;
import com.mishoo.dates.schemas.DateResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateService {

    private final DateRepository dateRepository;
    private final ClientAdapter clientAdapter;


    public Optional<DateResponse> getById(Long id) {
        return dateRepository.findById(id)
                .map(DateResponse::fromEntity);
    }

    public List<DateResponse> getAll() {
        return dateRepository.findAll()
                .stream()
                .map(DateResponse::fromEntity)
                .toList();
    }

    @Transactional

    public DateResponse save(DateRequest dateRequest) {
        clientAdapter.getById(dateRequest.client());

        Date createdDate = dateRepository.save(dateRequest.toEntity());

        return DateResponse.fromEntity(createdDate);
    }

    @Transactional
    public DateResponse change(Long id, DateRequest dateRequest) {

        clientAdapter.getById(dateRequest.client());

        Date date = dateRepository.findById(id)
                .orElseThrow(DateNotFoundException::new);

        date.updateFrom(dateRequest);
        Date updatedDate = dateRepository.save(date);
        return DateResponse.fromEntity(updatedDate);
    }

    @Transactional
    public void delete(Long id) {
        Date date = dateRepository.findById(id)
                .orElseThrow(DateNotFoundException::new);

        dateRepository.delete(date);
    }



}
