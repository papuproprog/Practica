package com.mishoo.clients.services;

import com.mishoo.clients.exceptions.ClientAlreadyExistsException;
import com.mishoo.clients.exceptions.ClientNotFoundException;
import com.mishoo.clients.exceptions.ClientPhoneAlreadyExistsException;
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
        if (clientRepository.existsByNameIgnoreCase(newClient.name().trim())) {
            throw new ClientAlreadyExistsException("Client with name '" + newClient.name() + "' already exists");
        }

        if (newClient.phone() != null && clientRepository.existsByPhone(newClient.phone().trim())) {
            throw new ClientPhoneAlreadyExistsException("Client with phone '" + newClient.phone() + "' already exists");
        }

        Client client = clientRepository.save(newClient.toEntity());
        return ClientResponse.fromEntity(client);
    }

    @Transactional
    public ClientResponse change(Long id, ClientRequest clientRequest) {
        Client client = clientRepository.findById(id)
                .orElseThrow(ClientNotFoundException::new);

        if (clientRepository.existsByNameIgnoreCaseAndIdNot(clientRequest.name().trim(), id)) {
            throw new ClientAlreadyExistsException("Client with name '" + clientRequest.name() + "' already exists");
        }

        if (clientRequest.phone() != null && clientRepository.existsByPhoneAndIdNot(clientRequest.phone().trim(), id)) {
            throw new ClientPhoneAlreadyExistsException("Client with phone '" + clientRequest.phone() + "' already exists");
        }

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
