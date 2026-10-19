package com.mishoo.clients.schemas;

import com.mishoo.clients.models.Client;

public record ClientResponse(Long id, String name , String lastName, String phone) {

    public static ClientResponse fromEntity(Client client){
        return new ClientResponse(client.getId(), client.getName(), client.getLastName(), client.getPhone());
    }

}
