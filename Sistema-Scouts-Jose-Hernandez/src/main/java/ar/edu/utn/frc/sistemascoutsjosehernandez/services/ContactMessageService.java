package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessage;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing contact messages from the website contact form
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    /**
     * Submit a new contact message from the website form
     */
    public ContactMessageDto submitContactMessage(CreateContactMessageDto createDto) {
        log.info("Processing new contact message from: {}", createDto.getEmail());

        try {
            // Create and save the contact message
            ContactMessage contactMessage = ContactMessage.builder()
                    .name(createDto.getName())
                    .email(createDto.getEmail())
                    .subject(createDto.getSubject())
                    .message(createDto.getMessage())
                    .messageType(createDto.getMessageType())
                    .status(ContactMessageStatus.NEW)
                    .build();

            ContactMessage savedMessage = contactMessageRepository.save(contactMessage);
            log.info("Contact message saved with ID: {}", savedMessage.getId());

            // Send notification email to admin (async)
            try {
                emailService.sendContactNotificationToAdmin(savedMessage);
            } catch (Exception e) {
                log.error("Failed to send admin notification for contact message {}: {}", 
                         savedMessage.getId(), e.getMessage());
                // Don't fail the entire operation if email fails
            }

            // Send auto-reply to user (async)
            try {
                emailService.sendContactAutoReply(savedMessage);
            } catch (Exception e) {
                log.error("Failed to send auto-reply for contact message {}: {}", 
                         savedMessage.getId(), e.getMessage());
                // Don't fail the entire operation if email fails
            }

            return mapToDto(savedMessage);

        } catch (Exception e) {
            log.error("Error processing contact message from {}: {}", createDto.getEmail(), e.getMessage());
            throw new RuntimeException("Error al procesar el mensaje de contacto", e);
        }
    }

    /**
     * Get all contact messages with pagination and filtering (admin only)
     */
    @Transactional(readOnly = true)
    public Page<ContactMessageDto> getContactMessages(ContactMessageFilterDto filterDto) {
        log.debug("Fetching contact messages with filters: {}", filterDto);

        Sort sort = Sort.by(
            filterDto.getSortDirection().equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC,
            filterDto.getSortBy()
        );

        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize(), sort);

        Page<ContactMessage> messagesPage = contactMessageRepository.findWithFilters(
            filterDto.getStatus(),
            filterDto.getMessageType(),
            filterDto.getStartDate(),
            filterDto.getEndDate(),
            filterDto.getSearchTerm(),
            pageable
        );

        return messagesPage.map(this::mapToDto);
    }

    /**
     * Get a specific contact message by ID (admin only)
     */
    @Transactional(readOnly = true)
    public Optional<ContactMessageDto> getContactMessageById(Long id) {
        log.debug("Fetching contact message with ID: {}", id);
        return contactMessageRepository.findById(id)
                .map(this::mapToDto);
    }

    /**
     * Update contact message status and admin notes (admin only)
     */
    public ContactMessageDto updateContactMessageStatus(Long id, UpdateContactMessageStatusDto updateDto) {
        log.info("Updating status for contact message {}: {}", id, updateDto.getStatus());

        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje de contacto no encontrado"));

        // Update status
        ContactMessageStatus previousStatus = message.getStatus();
        message.setStatus(updateDto.getStatus());
        
        // Update admin notes if provided
        if (updateDto.getAdminNotes() != null) {
            message.setAdminNotes(updateDto.getAdminNotes());
        }

        // Set replied timestamp if status changed to REPLIED
        if (updateDto.getStatus() == ContactMessageStatus.REPLIED && 
            previousStatus != ContactMessageStatus.REPLIED) {
            message.setRepliedAt(LocalDateTime.now());
        }

        ContactMessage savedMessage = contactMessageRepository.save(message);
        log.info("Contact message {} status updated to {}", id, updateDto.getStatus());

        return mapToDto(savedMessage);
    }

    /**
     * Mark message as read (admin only)
     */
    public ContactMessageDto markAsRead(Long id) {
        log.debug("Marking contact message {} as read", id);

        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje de contacto no encontrado"));

        if (message.getStatus() == ContactMessageStatus.NEW) {
            message.setStatus(ContactMessageStatus.READ);
            ContactMessage savedMessage = contactMessageRepository.save(message);
            return mapToDto(savedMessage);
        }

        return mapToDto(message);
    }

    /**
     * Get count of unread messages (admin only)
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return contactMessageRepository.countByStatus(ContactMessageStatus.NEW);
    }

    /**
     * Get contact message statistics (admin only)
     */
    @Transactional(readOnly = true)
    public ContactStatisticsDto getContactStatistics() {
        log.debug("Calculating contact message statistics");

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Object[] stats = contactMessageRepository.getContactStatistics(sevenDaysAgo);

        // Parse the native query results
        long total = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        long unread = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
        long replied = stats[2] != null ? ((Number) stats[2]).longValue() : 0;
        long recent = stats[3] != null ? ((Number) stats[3]).longValue() : 0;

        // Calculate archived (total - unread - replied)
        long archived = contactMessageRepository.countByStatus(ContactMessageStatus.ARCHIVED);

        return ContactStatisticsDto.builder()
                .totalMessages(total)
                .unreadMessages(unread)
                .repliedMessages(replied)
                .recentMessages(recent)
                .archivedMessages(archived)
                .build();
    }

    /**
     * Get messages from specific email address (admin only)
     */
    @Transactional(readOnly = true)
    public List<ContactMessageDto> getMessagesByEmail(String email) {
        log.debug("Fetching messages from email: {}", email);
        return contactMessageRepository.findByEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Delete a contact message (admin only)
     */
    public void deleteContactMessage(Long id) {
        log.info("Deleting contact message with ID: {}", id);
        
        if (!contactMessageRepository.existsById(id)) {
            throw new RuntimeException("Mensaje de contacto no encontrado");
        }
        
        contactMessageRepository.deleteById(id);
        log.info("Contact message {} deleted successfully", id);
    }

    /**
     * Map ContactMessage entity to DTO
     */
    private ContactMessageDto mapToDto(ContactMessage message) {
        return ContactMessageDto.builder()
                .id(message.getId())
                .name(message.getName())
                .email(message.getEmail())
                .subject(message.getSubject())
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .adminNotes(message.getAdminNotes())
                .repliedAt(message.getRepliedAt())
                .build();
    }
}