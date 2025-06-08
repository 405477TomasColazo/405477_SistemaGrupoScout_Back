package ar.edu.utn.frc.sistemascoutsjosehernandez.util;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.CompetenceProgress;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.progression.MarchSheet;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.CompetenceProgressRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.progression.MarchSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    
    private final MemberRepository memberRepository;
    private final MarchSheetRepository marchSheetRepository;
    private final CompetenceProgressRepository competenceProgressRepository;

    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public boolean isOwnerOrFamily(Authentication authentication, Integer memberId) {
        User currentUser = (User) authentication.getPrincipal();
        
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isEmpty()) {
            return false;
        }
        
        return member.get().getFamilyGroup().getMembers().stream()
                .anyMatch(m -> m.getUser() != null && m.getUser().getId().equals(currentUser.getId()));
    }

    public boolean isMarchSheetOwnerOrFamily(Authentication authentication, Integer marchSheetId) {
        Optional<MarchSheet> marchSheet = marchSheetRepository.findById(marchSheetId);
        if (marchSheet.isEmpty()) {
            return false;
        }
        
        return isOwnerOrFamily(authentication, marchSheet.get().getMember().getId());
    }

    public boolean isCompetenceProgressOwnerOrFamily(Authentication authentication, Integer progressId) {
        Optional<CompetenceProgress> progress = competenceProgressRepository.findById(progressId);
        if (progress.isEmpty()) {
            return false;
        }
        
        return isOwnerOrFamily(authentication, progress.get().getMarchSheet().getMember().getId());
    }
}
