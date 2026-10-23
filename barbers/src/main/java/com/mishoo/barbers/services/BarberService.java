package com.mishoo.barbers.services;

import com.mishoo.barbers.exceptions.BarberAlreadyExistsException;
import com.mishoo.barbers.exceptions.BarberNotFoundException;
import com.mishoo.barbers.models.Barber;
import com.mishoo.barbers.repositories.BarberRepository;
import com.mishoo.barbers.schema.BarberRequest;
import com.mishoo.barbers.schema.BarberResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;

    public List<BarberResponse> getAll() {
        return barberRepository.findAll()
                .stream()
                .map(BarberResponse::fromEntity)
                .toList();
    }

    public Optional<BarberResponse> getById(Long id) {
        return barberRepository.findById(id)
                .map(BarberResponse::fromEntity);
    }

    @Transactional
    public BarberResponse save(BarberRequest barberRequest) {
        if (barberRepository.existsByNameIgnoreCase(barberRequest.name().trim())) {
            throw new BarberAlreadyExistsException("Barber with name '" + barberRequest.name() + "' already exists");
        }

        Barber barber = barberRepository.save(barberRequest.toEntity());
        return BarberResponse.fromEntity(barber);
    }

    @Transactional
    public BarberResponse change(Long id, BarberRequest barberRequest) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(BarberNotFoundException::new);

        if (barberRepository.existsByNameIgnoreCaseAndIdNot(barberRequest.name().trim(), id)) {
            throw new BarberAlreadyExistsException("Barber with name '" + barberRequest.name() + "' already exists");
        }

        barber.updateFrom(barberRequest);

        Barber updatedBarber = barberRepository.save(barber);

        return BarberResponse.fromEntity(updatedBarber);
    }

    @Transactional
    public void delete(Long id) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(BarberNotFoundException::new);

        barberRepository.delete(barber);
    }

}
