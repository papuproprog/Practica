package com.mishoo.dates.controllers;

import com.mishoo.dates.schemas.DateRequest;
import com.mishoo.dates.schemas.DateResponse;
import com.mishoo.dates.services.DateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dates")
@RequiredArgsConstructor
public class DateController {

    private final DateService dateService;


    @GetMapping("/{id}")
    public ResponseEntity<DateResponse> retrieve(@PathVariable Long id) {
        return dateService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("")
    public ResponseEntity<List<DateResponse>> all() {
        return ResponseEntity.ok(dateService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<DateResponse> create(@Valid @RequestBody DateRequest dateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dateService.save(dateRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DateResponse> update(@PathVariable Long id, @Valid @RequestBody DateRequest dateRequest) {
        return ResponseEntity.ok(dateService.change(id, dateRequest));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        dateService.delete(id);
        return ResponseEntity.noContent().build();

    }


}
