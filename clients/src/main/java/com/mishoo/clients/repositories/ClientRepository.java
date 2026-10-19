package com.mishoo.clients.repositories;

import com.mishoo.clients.models.Client;
import com.mishoo.clients.schemas.ClientResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
