package com.mishoo.dates.services;

import com.mishoo.dates.adapters.BarberAdapter;
import com.mishoo.dates.adapters.ClientAdapter;
import com.mishoo.dates.adapters.ServiceAdapter;
import com.mishoo.dates.exceptions.*;
import com.mishoo.dates.models.Date;
import com.mishoo.dates.repositories.DateRepository;
import com.mishoo.dates.schemas.DataStatus;
import com.mishoo.dates.schemas.DateRequest;
import com.mishoo.dates.schemas.DateResponse;
import com.mishoo.dates.schemas.ServiceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateService {

    private final DateRepository dateRepository;
    private final ClientAdapter clientAdapter;
    private final BarberAdapter barberAdapter;
    private final ServiceAdapter serviceAdapter;

    public Optional<DateResponse> getById(Long id) {
        return dateRepository.findById(id)
                .map(DateResponse::fromEntity);
    }

    public List<DateResponse> getAll() {
        return dateRepository.findAll()
                .stream()
                .map(DateResponse::fromEntity)
                .toList();
    }

    @Transactional
    public DateResponse save(DateRequest dateRequest) {
        // Validar existencia de cliente, barbero y servicio en sus respectivos microservicios
        clientAdapter.getById(dateRequest.client());
        barberAdapter.getById(dateRequest.barber());
        ServiceResponse serviceResponse = serviceAdapter.getById(dateRequest.service());
        int duration = (serviceResponse != null && serviceResponse.durationMinutes() != null && serviceResponse.durationMinutes() > 0)
                ? serviceResponse.durationMinutes()
                : 30;

        validateDateRangeAndNotice(dateRequest.dateTime());
        validateBusinessHours(dateRequest.dateTime(), duration);
        validateClientAvailability(dateRequest.client(), dateRequest.dateTime(), null);
        validateBarberAvailabilityAndOverlap(dateRequest.barber(), dateRequest.dateTime(), duration, null);

        Date dateEntity = dateRequest.toEntity();
        dateEntity.setServiceDuration(duration);
        Date createdDate = dateRepository.save(dateEntity);

        return DateResponse.fromEntity(createdDate);
    }

    @Transactional
    public DateResponse change(Long id, DateRequest dateRequest) {
        Date date = dateRepository.findById(id)
                .orElseThrow(DateNotFoundException::new);

        boolean scheduleChanged = !Objects.equals(date.getBarber(), dateRequest.barber())
                || !Objects.equals(date.getClient(), dateRequest.client())
                || !Objects.equals(date.getService(), dateRequest.service())
                || !Objects.equals(date.getDateTime(), dateRequest.dateTime());

        int duration = date.getServiceDuration() != null ? date.getServiceDuration() : 30;

        if (scheduleChanged) {
            clientAdapter.getById(dateRequest.client());
            barberAdapter.getById(dateRequest.barber());
            ServiceResponse serviceResponse = serviceAdapter.getById(dateRequest.service());
            duration = (serviceResponse != null && serviceResponse.durationMinutes() != null && serviceResponse.durationMinutes() > 0)
                    ? serviceResponse.durationMinutes()
                    : 30;

            validateDateRangeAndNotice(dateRequest.dateTime());
            validateBusinessHours(dateRequest.dateTime(), duration);
            validateClientAvailability(dateRequest.client(), dateRequest.dateTime(), id);
            validateBarberAvailabilityAndOverlap(dateRequest.barber(), dateRequest.dateTime(), duration, id);
        }

        date.updateFrom(dateRequest);
        date.setServiceDuration(duration);
        Date updatedDate = dateRepository.save(date);
        return DateResponse.fromEntity(updatedDate);
    }

    @Transactional
    public void delete(Long id) {
        Date date = dateRepository.findById(id)
                .orElseThrow(DateNotFoundException::new);

        dateRepository.delete(date);
    }

    // Regla 4: Anticipación mínima (30 min) y ventana máxima futura (60 días)
    private void validateDateRangeAndNotice(LocalDateTime dateTime) {
        if (dateTime == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (dateTime.isBefore(now.plusMinutes(30))) {
            throw new InsufficientAdvanceNoticeException(
                    "Appointments must be scheduled with at least 30 minutes in advance"
            );
        }

        if (dateTime.isAfter(now.plusDays(60))) {
            throw new DateTooFarInFutureException(
                    "Appointments cannot be scheduled more than 60 days in advance"
            );
        }
    }

    // Regla de negocio: Horario de atención (08:00 a 20:00) considerando fin del servicio
    private void validateBusinessHours(LocalDateTime dateTime, int durationMinutes) {
        if (dateTime == null) {
            return;
        }

        int startHour = dateTime.getHour();
        if (startHour < 8 || startHour >= 20) {
            throw new OutsideBusinessHoursException(
                    "Appointments cannot be scheduled during early morning or night hours. Business hours are 08:00 to 20:00"
            );
        }

        LocalDateTime endDateTime = dateTime.plusMinutes(durationMinutes);
        if (endDateTime.getHour() > 20 || (endDateTime.getHour() == 20 && endDateTime.getMinute() > 0)) {
            throw new OutsideBusinessHoursException(
                    "The selected service ends after closing time (20:00). Please select an earlier time."
            );
        }
    }

    // Regla 1: Un cliente no puede tener dos citas a la misma hora
    private void validateClientAvailability(Long clientId, LocalDateTime dateTime, Long excludeDateId) {
        if (clientId == null || dateTime == null) {
            return;
        }

        boolean conflict = (excludeDateId == null)
                ? dateRepository.existsByClientAndDateTimeAndStatusNot(clientId, dateTime, DataStatus.CANCELLED)
                : dateRepository.existsByClientAndDateTimeAndStatusNotAndIdNot(clientId, dateTime, DataStatus.CANCELLED, excludeDateId);

        if (conflict) {
            throw new ClientAlreadyBookedException(
                    "Client (ID: " + clientId + ") already has an active appointment at " + dateTime
            );
        }
    }

    // Regla 2: Solapamiento de citas considerando duración del servicio para el barbero
    private void validateBarberAvailabilityAndOverlap(Long barberId, LocalDateTime dateTime, int requestedDuration, Long excludeDateId) {
        if (barberId == null || dateTime == null) {
            return;
        }

        LocalDateTime newStart = dateTime;
        LocalDateTime newEnd = dateTime.plusMinutes(requestedDuration);

        List<Date> existingAppointments = (excludeDateId == null)
                ? dateRepository.findByBarberAndStatusNot(barberId, DataStatus.CANCELLED)
                : dateRepository.findByBarberAndStatusNotAndIdNot(barberId, DataStatus.CANCELLED, excludeDateId);

        for (Date existing : existingAppointments) {
            LocalDateTime existingStart = existing.getDateTime();
            int existingDuration = existing.getServiceDuration() != null ? existing.getServiceDuration() : 30;
            LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);

            // Dos intervalos [A, B] y [C, D] se solapan si A < D y B > C
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                throw new BarberTimeSlotOverlapException(
                        "The appointment (" + newStart.toLocalTime() + " - " + newEnd.toLocalTime() +
                        ") overlaps with an existing appointment (" + existingStart.toLocalTime() + " - " + existingEnd.toLocalTime() + ") for barber ID " + barberId
                );
            }
        }
    }

}
