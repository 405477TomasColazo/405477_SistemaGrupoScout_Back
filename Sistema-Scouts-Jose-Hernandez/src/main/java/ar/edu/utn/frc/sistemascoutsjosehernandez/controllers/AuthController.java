package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.AuthResponse;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.LoginRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.RegisterRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.security.jwt.JwtService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.AuthService;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse>register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @GetMapping("/auth/invite/{token}")
    public ResponseEntity<?> validateInvitation(@PathVariable String token) {
        if (!jwtService.isInvitationTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o expirado");
        }

        String email = jwtService.getEmailFromToken(token);
        return ResponseEntity.ok(email);
    }
}
