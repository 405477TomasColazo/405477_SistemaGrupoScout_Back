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
    @PreAuthorize("hasAnyRole('FAMILY', 'EDUCATOR', 'ADMIN')")
    public ResponseEntity<FamilyGroupDto> getFamilyGroupById(@PathVariable Integer userId) {
        return ResponseEntity.ok(familyGroupService.getFamilyGroup(userId));
    }

    @PostMapping("/tutor")
    @PreAuthorize("hasAnyRole('FAMILY', 'ADMIN')")
    public ResponseEntity<TutorDto> addTutorToFamilyGroup(@RequestBody TutorDto tutor) {
        return ResponseEntity.ok(familyGroupService.addTutorToFamilyGroup(tutor));
    }

    @PostMapping("/member")
    @PreAuthorize("hasAnyRole('FAMILY', 'ADMIN')")
    public ResponseEntity<MemberDto> addMemberToFamilyGroup(@RequestBody MemberDto member) {
        return ResponseEntity.ok(familyGroupService.addMemberToFamilyGroup(member));
    }

    @PostMapping("/relationship")
    @PreAuthorize("hasAnyRole('FAMILY', 'ADMIN')")
    public ResponseEntity<RelationshipDto> addRelationshipToFamilyGroup(@RequestBody RelationshipDto relationship) {
        return ResponseEntity.ok(familyGroupService.addRelationship(relationship));
    }

    @PutMapping("/tutor/{id}")
    @PreAuthorize("hasAnyRole('FAMILY','ADMIN')")
    public ResponseEntity<TutorDto> updateTutor(@PathVariable Integer id, @RequestBody TutorDto tutor) {
        return ResponseEntity.ok(familyGroupService.updateTutor(tutor, id));
    }

    @PutMapping("/member/{id}")
    @PreAuthorize("hasAnyRole('FAMILY','ADMIN')")
    public ResponseEntity<MemberDto> updateMember(@PathVariable Integer id, @RequestBody MemberDto member) {
        return ResponseEntity.ok(familyGroupService.updateMember(member, id));
    }

    @PutMapping("/relationShip/{id}")
    @PreAuthorize("hasAnyRole('FAMILY','ADMIN')")
    public ResponseEntity<RelationshipDto> updateRelationship(@PathVariable Integer id, @RequestBody RelationshipDto relationship) {
        return ResponseEntity.ok(familyGroupService.updateRelationship(relationship, id));
    }
}
