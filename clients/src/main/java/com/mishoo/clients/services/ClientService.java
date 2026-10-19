package com.mishoo.clients.services;

import com.mishoo.clients.exceptions.ClientNotFoundException;
import com.mishoo.clients.models.Client;
import com.mishoo.clients.repositories.ClientRepository;
import com.mishoo.clients.schemas.ClientRequest;
import com.mishoo.clients.schemas.ClientResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<ClientResponse> getAll() {
        return clientRepository.findAll()
                .stream()
                .map(ClientResponse::fromEntity)
                .toList();
    }

    public Optional<ClientResponse> getById(Long id) {
        return clientRepository.findById(id)
                .map(ClientResponse::fromEntity);
    }

    @Transactional
    public ClientResponse save(ClientRequest newClient) {
        Client client = clientRepository.save(newClient.toEntity());
        return ClientResponse.fromEntity(client);
    }

    @Transactional
    public ClientResponse change(Long id, ClientRequest clientRequest) {
        Client client = clientRepository.findById(id)
                .orElseThrow(ClientNotFoundException::new);

        client.updateFrom(clientRequest);

        Client updatedClient = clientRepository.save(client);

        return ClientResponse.fromEntity(updatedClient);
    }

    @Transactional
    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(ClientNotFoundException::new);

        clientRepository.delete(client);
    }


}
