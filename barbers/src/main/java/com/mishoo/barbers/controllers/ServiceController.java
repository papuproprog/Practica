package com.mishoo.barbers.controllers;

import com.mishoo.barbers.schema.ServiceRequest;
import com.mishoo.barbers.schema.ServiceResponse;
import com.mishoo.barbers.services.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
public class ServiceController {

    private final CatalogService catalogService;

    @GetMapping("")
    public ResponseEntity<List<ServiceResponse>> all() {
        return ResponseEntity.ok(catalogService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> retrieve(@PathVariable Long id) {
        return catalogService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        return new ResponseEntity<>(catalogService.save(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(catalogService.change(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
