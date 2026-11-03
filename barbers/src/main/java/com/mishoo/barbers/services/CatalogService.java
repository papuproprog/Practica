package com.mishoo.barbers.services;

import com.mishoo.barbers.exceptions.ServiceNotFoundException;
import com.mishoo.barbers.models.ServiceItem;
import com.mishoo.barbers.repositories.ServiceRepository;
import com.mishoo.barbers.schema.ServiceRequest;
import com.mishoo.barbers.schema.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAll() {
        return serviceRepository.findAll()
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ServiceResponse> getById(Long id) {
        return serviceRepository.findById(id)
                .map(ServiceResponse::fromEntity);
    }

    @Transactional
    public ServiceResponse save(ServiceRequest request) {
        ServiceItem item = request.toEntity();
        ServiceItem saved = serviceRepository.save(item);
        return ServiceResponse.fromEntity(saved);
    }

    @Transactional
    public ServiceResponse change(Long id, ServiceRequest request) {
        ServiceItem item = serviceRepository.findById(id)
                .orElseThrow(ServiceNotFoundException::new);
        item.updateFrom(request);
        ServiceItem updated = serviceRepository.save(item);
        return ServiceResponse.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new ServiceNotFoundException();
        }
        serviceRepository.deleteById(id);
    }
}
