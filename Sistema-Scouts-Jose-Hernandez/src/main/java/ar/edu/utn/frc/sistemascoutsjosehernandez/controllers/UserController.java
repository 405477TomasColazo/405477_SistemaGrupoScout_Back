package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.UserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.NewUserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDto> getUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewUserDto> sendInvite(@RequestBody NewUserDto user) {
        return ResponseEntity.ok(userService.inviteUser(user));
    }
}
