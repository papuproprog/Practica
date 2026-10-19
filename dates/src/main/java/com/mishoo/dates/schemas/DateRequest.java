package com.mishoo.dates.schemas;
import com.mishoo.dates.models.Date;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DateRequest(

        @NotNull(message = "Barber can not be empty")
        Long barber,

        @NotNull(message = "Client can not be empty")
        Long client,

        @NotNull
        DataStatus status,

        @Future(message = "Past dates are not allowed")
        LocalDateTime dateTime


) {

    public Date toEntity() {
        Date date = new Date();
        date.setBarber(this.barber);
        date.setClient(this.client);
        date.setStatus(this.status);
        date.setDateTime(this.dateTime);
        return date;
    }


}
