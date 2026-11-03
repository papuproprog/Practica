package com.mishoo.barbers.models;

import com.mishoo.barbers.schema.ServiceRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "services")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del servicio no puede estar vacío")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "La duración en minutos es obligatoria")
    @Column(nullable = false)
    private Integer durationMinutes;

    @NotNull(message = "El precio es obligatorio")
    @Column(nullable = false)
    private Double price;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void updateFrom(ServiceRequest request) {
        if (request.name() != null && !request.name().isBlank()) {
            this.name = request.name();
        }
        if (request.description() != null) {
            this.description = request.description();
        }
        if (request.durationMinutes() != null) {
            this.durationMinutes = request.durationMinutes();
        }
        if (request.price() != null) {
            this.price = request.price();
        }
    }
}
