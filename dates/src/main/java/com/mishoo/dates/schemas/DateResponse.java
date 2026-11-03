package com.mishoo.dates.schemas;

import com.mishoo.dates.models.Date;

import java.time.LocalDateTime;

public record DateResponse(
        Long id,
        Long barber,
        Long client,
        DataStatus status,
        Long service,
        Integer serviceDuration,
        LocalDateTime dateTime
) {

    public static DateResponse fromEntity(Date date) {
        return new DateResponse(
                date.getId(),
                date.getBarber(),
                date.getClient(),
                date.getStatus(),
                date.getService(),
                date.getServiceDuration() != null ? date.getServiceDuration() : 30,
                date.getDateTime()
        );
    }

}
