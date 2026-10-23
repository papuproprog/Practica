package com.mishoo.barbers.schema;

import com.mishoo.barbers.models.Barber;

public record BarberResponse(

        Long id,

        String name,

        String lastName
) {

    public static BarberResponse fromEntity(Barber barber) {
        return new BarberResponse(barber.getId(), barber.getName(), barber.getLastName());


    }


}
