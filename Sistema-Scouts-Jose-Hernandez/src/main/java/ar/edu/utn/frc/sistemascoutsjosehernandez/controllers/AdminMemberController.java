package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.SectionMemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.UpdateAccountBalanceRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberController {

    private final MemberService memberService;

    @PutMapping("/{memberId}/balance")
    public ResponseEntity<SectionMemberDto> updateMemberAccountBalance(
            @PathVariable Integer memberId,
            @Valid @RequestBody UpdateAccountBalanceRequest request) {
        
        SectionMemberDto updatedMember = memberService.updateMemberAccountBalance(memberId, request);
        return ResponseEntity.ok(updatedMember);
    }
}