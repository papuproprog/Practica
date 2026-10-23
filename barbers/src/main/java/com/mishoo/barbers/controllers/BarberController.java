package com.mishoo.barbers.controllers;

import com.mishoo.barbers.schema.BarberRequest;
import com.mishoo.barbers.schema.BarberResponse;
import com.mishoo.barbers.services.BarberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/barbers")
public class BarberController {

    private final BarberService barberService;

    @GetMapping("/{id}")
    public ResponseEntity<BarberResponse> retrieve(@PathVariable Long id) {
        return barberService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("")
    public ResponseEntity<List<BarberResponse>> all() {
        return ResponseEntity.ok(barberService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<BarberResponse> create(@Valid @RequestBody BarberRequest barberRequest) {
        return new ResponseEntity<>(barberService.save(barberRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberResponse> update(@PathVariable Long id, @Valid @RequestBody BarberRequest barberRequest) {
        return ResponseEntity.ok(barberService.change(id, barberRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        barberService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
