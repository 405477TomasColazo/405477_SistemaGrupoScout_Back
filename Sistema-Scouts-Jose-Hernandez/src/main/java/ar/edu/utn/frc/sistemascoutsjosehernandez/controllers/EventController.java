package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.RegistrationStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.EventRegistrationService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.EventService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final EventRegistrationService eventRegistrationService;

    // GET /api/events - Obtener todos los eventos con filtros opcionales
    @GetMapping
    public ResponseEntity<List<EventDTO>> getEvents(
            @RequestParam(required = false) String sections,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Integer createdBy
    ) {
        EventFilterDTO filter = new EventFilterDTO();

        // Procesar sections (puede venir como "manada,unidad,caminantes")
        if (sections != null && !sections.isEmpty()) {
            filter.setSections(Set.of(sections.split(",")));
        }

        if (eventType != null && !eventType.isEmpty()) {
            filter.setEventType(EventType.fromValue(eventType));
        }

        if (status != null && !status.isEmpty()) {
            filter.setStatus(EventStatus.fromValue(status));
        }

        filter.setDateFrom(dateFrom);
        filter.setDateTo(dateTo);
        filter.setCreatedBy(createdBy);

        List<EventDTO> events = eventService.getEvents(filter);
        return ResponseEntity.ok(events);
    }

    // GET /api/events/{id} - Obtener un evento específico
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Integer id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
    // POST /api/events - Crear nuevo evento
    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody CreateEventDTO createEventDTO) {
        EventDTO createdEvent = eventService.createEvent(createEventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    // PUT /api/events/{id} - Actualizar evento
    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEventDTO updateEventDTO
    ) {
        EventDTO updatedEvent = eventService.updateEvent(id, updateEventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    // DELETE /api/events/{id} - Eliminar evento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/events/{eventId}/registrations - Obtener registraciones de un evento
    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<List<EventRegistrationDTO>> getEventRegistrations(@PathVariable Integer eventId) {
        List<EventRegistrationDTO> registrations = eventRegistrationService.getEventRegistrations(eventId);
        return ResponseEntity.ok(registrations);
    }

    // POST /api/events/{eventId}/register - Registrar miembros a un evento
    @PostMapping("/{eventId}/register")
    public ResponseEntity<List<EventRegistrationDTO>> registerMembersToEvent(
            @PathVariable Integer eventId,
            @Valid @RequestBody RegisterMembersRequestDTO request
    ) {
        List<EventRegistrationDTO> registrations = eventRegistrationService.registerMembersToEvent(
                eventId,
                request.getMemberIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(registrations);
    }

    // PUT /api/events/{eventId}/registrations/{registrationId} - Actualizar estado de registración
    @PutMapping("/{eventId}/registrations/{registrationId}")
    public ResponseEntity<EventRegistrationDTO> updateRegistration(
            @PathVariable Integer eventId,
            @PathVariable Integer registrationId,
            @Valid @RequestBody UpdateRegistrationStatusDTO request
    ) {
        EventRegistrationDTO updatedRegistration = eventRegistrationService.updateRegistrationStatus(
                eventId,
                registrationId,
                request.getStatus()
        );
        return ResponseEntity.ok(updatedRegistration);
    }

    // POST /api/events/{eventId}/accept-invitation - Aceptar invitación a evento
//    @PostMapping("/{eventId}/accept-invitation")
//    public ResponseEntity<EventRegistrationDTO> acceptInvitation(@PathVariable Integer eventId) {
//        Long currentUserId = SecurityUtils.getCurrentUserId();
//        EventRegistrationDTO registration = eventRegistrationService.respondToInvitation(
//                eventId,
//                currentUserId,
//                RegistrationStatus.CONFIRMED
//        );
//        return ResponseEntity.ok(registration);
//    }
//
//    // POST /api/events/{eventId}/reject-invitation - Rechazar invitación a evento
//    @PostMapping("/{eventId}/reject-invitation")
//    public ResponseEntity<Void> rejectInvitation(@PathVariable Integer eventId) {
//        Integer currentUserId = SecurityUtils.getCurrentUserId();
//        eventRegistrationService.respondToInvitation(
//                eventId,
//                currentUserId,
//                RegistrationStatus.DECLINED
//        );
//        return ResponseEntity.ok().build();
//    }
}
