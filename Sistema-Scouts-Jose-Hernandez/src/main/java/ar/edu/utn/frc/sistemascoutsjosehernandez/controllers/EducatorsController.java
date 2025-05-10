package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers;


import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.SectionMemberDto;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.MemberService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/educators")
@RequiredArgsConstructor
public class EducatorsController {
    private final MemberService memberService;

    @GetMapping("/nomina")
    @PreAuthorize("hasAnyRole('EDUCATOR','ADMIN')")
    public ResponseEntity<List<SectionMemberDto>> getNomina() {
        User user = SecurityUtils.getCurrentUser();
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        List<SectionMemberDto> nomina;
        if (isAdmin) {
            nomina = memberService.getAllMembers();
        }else {
            nomina = memberService.getMembersByEducator(user.getUserId());
        }
        return ResponseEntity.ok(nomina);
    }
}
