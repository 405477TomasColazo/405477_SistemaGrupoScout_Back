package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.FamilyGroupDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FamilyGroup;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.FamilyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/familyGroup")
@RequiredArgsConstructor
public class FamilyGroupController {
    private final FamilyGroupService familyGroupService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FamilyGroupDto> getFamilyGroupById(@PathVariable Integer userId) {
        return ResponseEntity.ok(familyGroupService.getFamilyGroup(userId));
    }
}
