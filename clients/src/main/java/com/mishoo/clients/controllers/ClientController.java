package com.mishoo.clients.controllers;
import com.mishoo.clients.schemas.ClientRequest;
import com.mishoo.clients.schemas.ClientResponse;
import com.mishoo.clients.services.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;


    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> retrieve(@PathVariable Long id) {
        return clientService.getById(id)
                .map(ResponseEntity::ok).
                orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("")
    public ResponseEntity<List<ClientResponse>> list() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest newClient) {
        return new ResponseEntity<>(clientService.save(newClient), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@Valid @RequestBody ClientRequest newClient, @PathVariable Long id) {
        return ResponseEntity.ok(clientService.change(id, newClient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
