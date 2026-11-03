package com.mishoo.clients.schemas;

import com.mishoo.clients.models.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientRequest(
        @NotBlank(message = "Last name missing")
        @Size(min = 1, max = 50, message = "Name length not accepted")
        String name,

        @NotBlank(message = "Last name missing")
        @Size(min = 1, max = 50, message = "Last name length not accepted")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        String phone

) {

    public Client toEntity() {
        Client client = new Client();
        client.setName(this.name);
        client.setLastName(this.lastName);
        client.setPhone(this.phone);
        return client;
    }

    public String fullName() {
        return name + " " + lastName;
    }


}
