package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.FamilyGroupDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.MemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.RelationshipDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.TutorDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.FamilyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/tutor")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TutorDto> addTutorToFamilyGroup(@RequestBody TutorDto tutor) {
        return ResponseEntity.ok(familyGroupService.addTutorToFamilyGroup(tutor));
    }

    @PostMapping("/member")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemberDto> addMemberToFamilyGroup(@RequestBody MemberDto member) {
        return ResponseEntity.ok(familyGroupService.addMemberToFamilyGroup(member));
    }

    @PostMapping("/relationship")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RelationshipDto> addRelationshipToFamilyGroup(@RequestBody RelationshipDto relationship) {
        return ResponseEntity.ok(familyGroupService.addRelationship(relationship));
    }
}
