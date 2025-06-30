package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact.ContactMessageDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.contact.CreateContactMessageDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public controller for contact form submissions from the website
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contact", description = "Public contact form API")
public class ContactController {

    private final ContactMessageService contactMessageService;

    /**
     * Submit a contact form from the website (public endpoint)
     */
    @PostMapping
    @Operation(
        summary = "Submit contact form",
        description = "Submit a contact message from the website contact form. This is a public endpoint that doesn't require authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contact message submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Server error while processing the message")
    })
    public ResponseEntity<?> submitContactForm(@Valid @RequestBody CreateContactMessageDto createDto) {
        log.info("Received contact form submission from: {} ({})", createDto.getName(), createDto.getEmail());

        try {
            ContactMessageDto result = contactMessageService.submitContactMessage(createDto);
            
            log.info("Contact message submitted successfully with ID: {}", result.getId());
            
            // Return success response without exposing internal details
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ContactSubmissionResponse(
                    true,
                    "Mensaje enviado correctamente. Te responderemos pronto.",
                    result.getId()
                )
            );

        } catch (Exception e) {
            log.error("Error processing contact form submission: {}", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ContactSubmissionResponse(
                    false,
                    "Error al enviar el mensaje. Por favor, intenta nuevamente más tarde.",
                    null
                )
            );
        }
    }

    /**
     * Response DTO for contact form submissions
     */
    public record ContactSubmissionResponse(
        boolean success,
        String message,
        Long contactId
    ) {}
}