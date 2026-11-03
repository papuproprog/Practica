package com.mishoo.barbers.schema;

import com.mishoo.barbers.models.Barber;
import jakarta.validation.constraints.NotBlank;

public record BarberRequest(

        @NotBlank(message = "The name can not be empty")
        String name,

        @NotBlank(message = "The last name can not be empty")
        String lastName

) {

    public Barber toEntity() {
        Barber barber = new Barber();
        barber.setName(this.name);
        barber.setLastName(this.lastName);
        return barber;
    }


}
