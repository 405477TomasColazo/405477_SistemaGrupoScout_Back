package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.UserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.AuthResponse;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.LoginRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.RegisterRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.ForgotPasswordRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.ResetPasswordRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.security.jwt.JwtService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.AuthService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto>register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(userService.register(registerRequest));
    }

    @GetMapping("/auth/invite/{token}")
    public ResponseEntity<?> validateInvitation(@PathVariable String token) {
        if (!jwtService.isInvitationTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o expirado");
        }

        String email = jwtService.getEmailFromToken(token);
        return ResponseEntity.ok(email);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.forgotPassword(request);
            return ResponseEntity.ok("Se ha enviado un email con las instrucciones para recuperar tu contraseña");
        } catch (RuntimeException e) {
            // For security reasons, always return success even if email doesn't exist
            return ResponseEntity.ok("Se ha enviado un email con las instrucciones para recuperar tu contraseña");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request);
            return ResponseEntity.ok("Contraseña restablecida exitosamente");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/reset-password/validate/{token}")
    public ResponseEntity<Boolean> validateResetToken(@PathVariable String token) {
        boolean isValid = userService.validateResetToken(token);
        return ResponseEntity.ok(isValid);
    }
}
