package com.mishoo.clients.models;

import com.mishoo.clients.schemas.ClientRequest;
import com.mishoo.clients.schemas.ClientResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "clients")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    private String lastName;

    @NotNull
    private String phone;

    public void updateFrom(ClientRequest request) {
        this.name = request.name();
        this.lastName = request.lastName();
        this.phone = request.phone();
    }

}
