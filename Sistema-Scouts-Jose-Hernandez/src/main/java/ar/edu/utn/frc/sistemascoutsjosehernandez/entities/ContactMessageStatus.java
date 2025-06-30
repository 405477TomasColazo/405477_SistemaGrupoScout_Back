package ar.edu.utn.frc.sistemascoutsjosehernandez.entities;

/**
 * Status enum for contact messages from the website contact form
 */
public enum ContactMessageStatus {
    NEW,        // Just received, not yet read by admin
    READ,       // Admin has viewed the message
    REPLIED,    // Admin has replied to the user
    ARCHIVED    // Message has been archived/resolved
}