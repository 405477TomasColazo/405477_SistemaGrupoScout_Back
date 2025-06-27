package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageStatus;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.ContactMessageType;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin controller for managing contact messages
 */
@RestController
@RequestMapping("/admin/contact")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Contact Management", description = "Admin API for managing contact messages")
public class AdminContactController {

    private final ContactMessageService contactMessageService;

    /**
     * Get all contact messages with filtering and pagination
     */
    @GetMapping("/messages")
    @Operation(
        summary = "Get contact messages",
        description = "Get all contact messages with optional filtering and pagination. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact messages retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Page<ContactMessageDto>> getContactMessages(
            @RequestParam(required = false) ContactMessageStatus status,
            @RequestParam(required = false) ContactMessageType messageType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Admin fetching contact messages - page: {}, size: {}, status: {}", page, size, status);

        ContactMessageFilterDto filterDto = ContactMessageFilterDto.builder()
                .status(status)
                .messageType(messageType)
                .startDate(startDate)
                .endDate(endDate)
                .searchTerm(searchTerm)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<ContactMessageDto> messages = contactMessageService.getContactMessages(filterDto);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get a specific contact message by ID
     */
    @GetMapping("/messages/{id}")
    @Operation(
        summary = "Get contact message by ID",
        description = "Get a specific contact message and mark it as read. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact message retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Contact message not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<ContactMessageDto> getContactMessage(@PathVariable Long id) {
        log.debug("Admin fetching contact message with ID: {}", id);

        return contactMessageService.getContactMessageById(id)
                .map(message -> {
                    // Automatically mark as read when admin views it
                    if (message.isUnread()) {
                        return ResponseEntity.ok(contactMessageService.markAsRead(id));
                    }
                    return ResponseEntity.ok(message);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update contact message status and admin notes
     */
    @PutMapping("/messages/{id}/status")
    @Operation(
        summary = "Update contact message status",
        description = "Update the status and admin notes of a contact message. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Contact message not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status data"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<ContactMessageDto> updateContactMessageStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactMessageStatusDto updateDto) {

        log.info("Admin updating status for contact message {}: {}", id, updateDto.getStatus());

        try {
            ContactMessageDto updatedMessage = contactMessageService.updateContactMessageStatus(id, updateDto);
            return ResponseEntity.ok(updatedMessage);
        } catch (RuntimeException e) {
            log.error("Error updating contact message status: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mark contact message as read
     */
    @PatchMapping("/messages/{id}/read")
    @Operation(
        summary = "Mark message as read",
        description = "Mark a contact message as read. Admin only."
    )
    public ResponseEntity<ContactMessageDto> markAsRead(@PathVariable Long id) {
        log.debug("Admin marking contact message {} as read", id);

        try {
            ContactMessageDto updatedMessage = contactMessageService.markAsRead(id);
            return ResponseEntity.ok(updatedMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get contact message statistics
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get contact statistics",
        description = "Get contact message statistics for admin dashboard. Admin only."
    )
    public ResponseEntity<ContactStatisticsDto> getContactStatistics() {
        log.debug("Admin fetching contact message statistics");

        ContactStatisticsDto statistics = contactMessageService.getContactStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    @Operation(
        summary = "Get unread message count",
        description = "Get the count of unread contact messages for notifications. Admin only."
    )
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        long unreadCount = contactMessageService.getUnreadCount();
        return ResponseEntity.ok(new UnreadCountResponse(unreadCount));
    }

    /**
     * Get messages from specific email address
     */
    @GetMapping("/messages/by-email")
    @Operation(
        summary = "Get messages by email",
        description = "Get all messages from a specific email address. Admin only."
    )
    public ResponseEntity<List<ContactMessageDto>> getMessagesByEmail(@RequestParam String email) {
        log.debug("Admin fetching messages from email: {}", email);

        List<ContactMessageDto> messages = contactMessageService.getMessagesByEmail(email);
        return ResponseEntity.ok(messages);
    }

    /**
     * Delete a contact message
     */
    @DeleteMapping("/messages/{id}")
    @Operation(
        summary = "Delete contact message",
        description = "Delete a contact message permanently. Admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Contact message not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public ResponseEntity<Void> deleteContactMessage(@PathVariable Long id) {
        log.info("Admin deleting contact message with ID: {}", id);

        try {
            contactMessageService.deleteContactMessage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Response DTO for unread count
     */
    public record UnreadCountResponse(long unreadCount) {}
}