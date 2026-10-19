package com.mishoo.dates.models;

import com.mishoo.dates.schemas.DataStatus;
import com.mishoo.dates.schemas.DateRequest;
import com.mishoo.dates.schemas.DateResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Date {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long barber;

    @NotNull
    private Long client;

    @Enumerated(EnumType.STRING)
    private DataStatus status = DataStatus.PENDING;

    @NotNull
    private LocalDateTime dateTime;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public void updateFrom(DateRequest dateRequest){
        this.barber = (dateRequest.barber());
        this.client = (dateRequest.client());
        this.status = (dateRequest.status());
        this.dateTime = (dateRequest.dateTime());

    }






}
