package com.mishoo.barbers.schema;

import com.mishoo.barbers.models.ServiceItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ServiceRequest(
        @NotBlank(message = "El nombre del servicio no puede estar vacío")
        String name,

        @NotBlank(message = "La descripción no puede estar vacía")
        String description,

        @NotNull(message = "La duración en minutos es requerida")
        @Positive(message = "La duración debe ser mayor a 0")
        Integer durationMinutes,

        @NotNull(message = "El precio es requerido")
        @Positive(message = "El precio debe ser mayor a 0")
        Double price
) {
    public ServiceItem toEntity() {
        ServiceItem item = new ServiceItem();
        item.setName(this.name);
        item.setDescription(this.description);
        item.setDurationMinutes(this.durationMinutes);
        item.setPrice(this.price);
        return item;
    }
}
