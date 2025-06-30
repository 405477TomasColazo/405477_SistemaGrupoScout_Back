package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.UserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.NewUserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.InvitationDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.UpdateProfileRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.ChangePasswordRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.UserService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.UserService.AvatarOption;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewUserDto> sendInvite(@RequestBody NewUserDto user) {
        return ResponseEntity.ok(userService.inviteUser(user));
    }

    @GetMapping("/invitations/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationDto>> getPendingInvitations() {
        return ResponseEntity.ok(userService.getPendingInvitations());
    }

    @GetMapping("/invitations/completed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationDto>> getCompletedRegistrations() {
        return ResponseEntity.ok(userService.getCompletedRegistrations());
    }

    @PostMapping("/invitations/{id}/resend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationDto> resendInvitation(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.resendInvitation(id));
    }

    @DeleteMapping("/invitations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInvitation(@PathVariable Integer id) {
        userService.deleteInvitation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Section>> getAllSections() {
        return ResponseEntity.ok(userService.getAllSections());
    }

    // Profile management endpoints
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        UserDto updatedUser = userService.updateUserProfile(email, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        userService.changePassword(email, request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @GetMapping("/avatars")
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<List<AvatarOption>> getAvailableAvatars() {
        List<AvatarOption> avatars = userService.getAvailableAvatars();
        return ResponseEntity.ok(avatars);
    }
}
