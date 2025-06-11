package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events.EventRegistrationDTO;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Fee;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.PaymentStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventRegistration;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.RegistrationStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.EventRegistrationRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.EventRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FeeRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventRegistrationService {
    
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final FeeRepository feeRepository;

    public List<EventRegistrationDTO> getEventRegistrations(Integer eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
        
        List<EventRegistration> registrations = eventRegistrationRepository.findByEventIdOrderByRegistrationDateAsc(eventId);
        
        return registrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EventRegistrationDTO> registerMembersToEvent(Integer eventId, @NotEmpty(message = "Member IDs cannot be empty") List<Integer> memberIds) {
        return registerMembersToEvent(eventId, memberIds, RegistrationStatus.CONFIRMED);
    }

    public List<EventRegistrationDTO> registerMembersToEvent(Integer eventId, @NotEmpty(message = "Member IDs cannot be empty") List<Integer> memberIds, RegistrationStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        List<EventRegistration> newRegistrations = new ArrayList<>();

        for (Integer memberId : memberIds) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

            // Check if registration already exists
            EventRegistration existingRegistration = eventRegistrationRepository
                    .findByEventIdAndMemberId(eventId, memberId).orElse(null);

            if (existingRegistration != null) {
                // Update existing registration if it was declined or cancelled
                if (existingRegistration.getStatus() == RegistrationStatus.DECLINED || 
                    existingRegistration.getStatus() == RegistrationStatus.CANCELLED) {
                    
                    RegistrationStatus oldStatus = existingRegistration.getStatus();
                    existingRegistration.setStatus(status);
                    newRegistrations.add(eventRegistrationRepository.save(existingRegistration));
                    
                    // Update capacity and create fees if confirming
                    if (status == RegistrationStatus.CONFIRMED && oldStatus != RegistrationStatus.CONFIRMED) {
                        event.setCurrentCapacity((event.getCurrentCapacity() != null ? event.getCurrentCapacity() : 0) + 1);
                        createEventFeeIfRequired(event, member);
                    }
                } else {
                    // If already pending or confirmed, skip
                    newRegistrations.add(existingRegistration);
                }
            } else {
                // Create new registration
                int currentCapacity = event.getCurrentCapacity() != null ? event.getCurrentCapacity() : 0;
                if (event.getMaxCapacity() != null && currentCapacity >= event.getMaxCapacity()) {
                    throw new RuntimeException("Event has reached maximum capacity");
                }

                EventRegistration registration = EventRegistration.builder()
                        .eventId(eventId)
                        .memberId(memberId)
                        .memberName(member.getName())
                        .memberLastName(member.getLastname())
                        .status(status)
                        .build();

                newRegistrations.add(eventRegistrationRepository.save(registration));
                event.setCurrentCapacity(currentCapacity + 1);
                
                // Create fee if confirming attendance
                if (status == RegistrationStatus.CONFIRMED) {
                    createEventFeeIfRequired(event, member);
                }
            }
        }

        eventRepository.save(event);

        return newRegistrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EventRegistrationDTO updateRegistrationStatus(Integer eventId, Integer registrationId, @NotNull(message = "Status is required") RegistrationStatus status) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }

        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + registrationId));

        if (!registration.getEventId().equals(eventId)) {
            throw new RuntimeException("Registration does not belong to the specified event");
        }

        RegistrationStatus oldStatus = registration.getStatus();
        registration.setStatus(status);

        Event event = eventRepository.findById(eventId).get();

        if (oldStatus == RegistrationStatus.CONFIRMED && status != RegistrationStatus.CONFIRMED) {
            event.setCurrentCapacity((event.getCurrentCapacity() != null ? event.getCurrentCapacity() : 0) - 1);
        } else if (oldStatus != RegistrationStatus.CONFIRMED && status == RegistrationStatus.CONFIRMED) {
            event.setCurrentCapacity((event.getCurrentCapacity() != null ? event.getCurrentCapacity() : 0) + 1);
            
            // Create fee if event requires payment
            Member member = memberRepository.findById(registration.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Member not found with id: " + registration.getMemberId()));
            createEventFeeIfRequired(event, member);
        }

        eventRepository.save(event);
        EventRegistration savedRegistration = eventRegistrationRepository.save(registration);

        return convertToDTO(savedRegistration);
    }
    
    
    private void createEventFeeIfRequired(Event event, Member member) {
        if (event.getRequiresPayment() != null && event.getRequiresPayment() && event.getCost() != null) {
            Fee eventFee = Fee.builder()
                    .member(member)
                    .description("Evento: " + event.getTitle())
                    .amount(event.getCost())
                    .period(event.getStartDate().toString())
                    .status(PaymentStatus.PENDING)
                    .build();
            feeRepository.save(eventFee);
        }
    }

    private EventRegistrationDTO convertToDTO(EventRegistration registration) {
        return EventRegistrationDTO.builder()
                .id(registration.getId())
                .eventId(registration.getEventId())
                .memberId(registration.getMemberId())
                .memberName(registration.getMemberName())
                .memberLastName(registration.getMemberLastName())
                .registrationDate(registration.getRegistrationDate())
                .status(registration.getStatus())
                .paymentStatus(registration.getPaymentStatus())
                .paymentId(registration.getPaymentId())
                .notes(registration.getNotes())
                .build();
    }
}
