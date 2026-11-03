package com.mishoo.barbers.schema;

import com.mishoo.barbers.models.ServiceItem;
import java.time.LocalDateTime;

public record ServiceResponse(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        Double price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ServiceResponse fromEntity(ServiceItem item) {
        return new ServiceResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getDurationMinutes(),
                item.getPrice(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
