package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.events.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.Event;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventAttachment;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.EventRegistration;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.InvitationType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.events.RegistrationStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.EventRegistrationRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.EventRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.SectionRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
    
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public List<EventDTO> getEvents(EventFilterDTO filter) {
        List<Event> events = eventRepository.findEventsWithFilters(
                filter.getSections(),
                filter.getEventType(),
                filter.getStatus(),
                filter.getDateFrom(),
                filter.getDateTo(),
                filter.getCreatedBy()
        );
        
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        return convertToDTO(event);
    }

    public EventDTO createEvent(@Valid CreateEventDTO createEventDTO) {
        Event event = Event.builder()
                .title(createEventDTO.getTitle())
                .description(createEventDTO.getDescription())
                .eventType(createEventDTO.getEventType())
                .startDate(createEventDTO.getStartDate())
                .endDate(createEventDTO.getEndDate())
                .location(createEventDTO.getLocation())
                .sections(createEventDTO.getSections())
                .cost(createEventDTO.getCost())
                .maxCapacity(createEventDTO.getMaxCapacity())
                .status(createEventDTO.getStatus())
                .createdBy(createEventDTO.getCreatedBy())
                .registrationDeadline(createEventDTO.getRegistrationDeadline())
                .invitationType(createEventDTO.getInvitationType())
                .invitedMembers(createEventDTO.getInvitedMembers())
                .requiresPayment(createEventDTO.getRequiresPayment())
                .paymentDeadline(createEventDTO.getPaymentDeadline())
                .notes(createEventDTO.getNotes())
                .build();

        Event savedEvent = eventRepository.save(event);
        
        // Create event registrations for invited members
        createEventRegistrations(savedEvent);
        
        // Send invitations after saving the event
        sendInvitations(savedEvent);
        
        return convertToDTO(savedEvent);
    }

    public EventDTO updateEvent(Long id, @Valid UpdateEventDTO updateEventDTO) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (updateEventDTO.getTitle() != null) {
            event.setTitle(updateEventDTO.getTitle());
        }
        if (updateEventDTO.getDescription() != null) {
            event.setDescription(updateEventDTO.getDescription());
        }
        if (updateEventDTO.getEventType() != null) {
            event.setEventType(updateEventDTO.getEventType());
        }
        if (updateEventDTO.getStartDate() != null) {
            event.setStartDate(updateEventDTO.getStartDate());
        }
        if (updateEventDTO.getEndDate() != null) {
            event.setEndDate(updateEventDTO.getEndDate());
        }
        if (updateEventDTO.getLocation() != null) {
            event.setLocation(updateEventDTO.getLocation());
        }
        if (updateEventDTO.getSections() != null) {
            event.setSections(updateEventDTO.getSections());
        }
        if (updateEventDTO.getCost() != null) {
            event.setCost(updateEventDTO.getCost());
        }
        if (updateEventDTO.getMaxCapacity() != null) {
            event.setMaxCapacity(updateEventDTO.getMaxCapacity());
        }
        if (updateEventDTO.getStatus() != null) {
            event.setStatus(updateEventDTO.getStatus());
        }
        if (updateEventDTO.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(updateEventDTO.getRegistrationDeadline());
        }
        if (updateEventDTO.getInvitationType() != null) {
            event.setInvitationType(updateEventDTO.getInvitationType());
        }
        if (updateEventDTO.getInvitedMembers() != null) {
            event.setInvitedMembers(updateEventDTO.getInvitedMembers());
        }
        if (updateEventDTO.getRequiresPayment() != null) {
            event.setRequiresPayment(updateEventDTO.getRequiresPayment());
        }
        if (updateEventDTO.getPaymentDeadline() != null) {
            event.setPaymentDeadline(updateEventDTO.getPaymentDeadline());
        }
        if (updateEventDTO.getNotes() != null) {
            event.setNotes(updateEventDTO.getNotes());
        }

        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    private EventDTO convertToDTO(Event event) {
        List<EventAttachmentDTO> attachmentDTOs = event.getAttachments() != null 
                ? event.getAttachments().stream()
                    .map(this::convertAttachmentToDTO)
                    .collect(Collectors.toList())
                : List.of();

        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .sections(event.getSections())
                .cost(event.getCost())
                .maxCapacity(event.getMaxCapacity())
                .currentCapacity(event.getCurrentCapacity())
                .status(event.getStatus())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .registrationDeadline(event.getRegistrationDeadline())
                .invitationType(event.getInvitationType())
                .invitedMembers(event.getInvitedMembers())
                .requiresPayment(event.getRequiresPayment())
                .paymentDeadline(event.getPaymentDeadline())
                .attachments(attachmentDTOs)
                .notes(event.getNotes())
                .build();
    }

    private EventAttachmentDTO convertAttachmentToDTO(EventAttachment attachment) {
        return EventAttachmentDTO.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
    
    private void sendInvitations(Event event) {
        Set<User> usersToInvite = getUsersToInvite(event);
        
        for (User user : usersToInvite) {
            try {
                // Create notification
                notificationService.createEventInvitationNotification(user, event);
                
                // Send email invitation
                emailService.sendEventInvitation(user, event);
            } catch (Exception e) {
                // Log error but don't stop the process for other users
                System.err.println("Error sending invitation to user " + user.getId() + ": " + e.getMessage());
            }
        }
    }
    
    private Set<User> getUsersToInvite(Event event) {
        Set<User> usersToInvite = new HashSet<>();
        
        if (event.getInvitationType() == InvitationType.ALL) {
            // Invite all members from the event's sections
            if (event.getSections() != null && !event.getSections().isEmpty()) {
                for (String sectionName : event.getSections()) {
                    sectionRepository.findByDescription(sectionName).ifPresent(section -> {
                        List<Member> sectionMembers = memberRepository.findAllBySection(section);
                        for (Member member : sectionMembers) {
                            if (member.getUser() != null) {
                                usersToInvite.add(member.getUser());
                            }
                        }
                    });
                }
            }
        } else if (event.getInvitationType() == InvitationType.SELECTED) {
            // Invite only selected members
            if (event.getInvitedMembers() != null && !event.getInvitedMembers().isEmpty()) {
                for (Integer memberId : event.getInvitedMembers()) {
                    memberRepository.findById(memberId).ifPresent(member -> {
                        if (member.getUser() != null) {
                            usersToInvite.add(member.getUser());
                        }
                    });
                }
            }
        }
        
        return usersToInvite;
    }
    
    private void createEventRegistrations(Event event) {
        Set<Member> membersToRegister = getMembersToRegister(event);
        
        for (Member member : membersToRegister) {
            try {
                // Check if registration already exists
                if (!eventRegistrationRepository.existsByEventIdAndMemberId(event.getId(), member.getId())) {
                    EventRegistration registration = EventRegistration.builder()
                            .eventId(event.getId())
                            .memberId(member.getId())
                            .memberName(member.getName())
                            .memberLastName(member.getLastname())
                            .status(RegistrationStatus.PENDING)
                            .build();
                    
                    eventRegistrationRepository.save(registration);
                }
            } catch (Exception e) {
                // Log error but don't stop the process for other members
                System.err.println("Error creating registration for member " + member.getId() + ": " + e.getMessage());
            }
        }
    }
    
    private Set<Member> getMembersToRegister(Event event) {
        Set<Member> membersToRegister = new HashSet<>();
        
        if (event.getInvitationType() == InvitationType.ALL) {
            // Register all members from the event's sections
            if (event.getSections() != null && !event.getSections().isEmpty()) {
                for (String sectionName : event.getSections()) {
                    sectionRepository.findByDescription(sectionName).ifPresent(section -> {
                        List<Member> sectionMembers = memberRepository.findAllBySection(section);
                        membersToRegister.addAll(sectionMembers);
                    });
                }
            }
        } else if (event.getInvitationType() == InvitationType.SELECTED) {
            // Register only selected members
            if (event.getInvitedMembers() != null && !event.getInvitedMembers().isEmpty()) {
                for (Integer memberId : event.getInvitedMembers()) {
                    memberRepository.findById(memberId).ifPresent(membersToRegister::add);
                }
            }
        }
        
        return membersToRegister;
    }
}
