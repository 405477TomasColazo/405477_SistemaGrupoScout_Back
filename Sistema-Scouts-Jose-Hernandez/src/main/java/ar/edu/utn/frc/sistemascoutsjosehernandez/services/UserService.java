package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.UserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.NewUserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.InvitationDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.RegisterRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final StatusRepository statusRepository;
    private final InvitationRepository invitationRepository;
    private final FamilyGroupRepository familyGroupRepository;
    private final MemberTypeRepository memberTypeRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final RolesXUserRepository rolesXUserRepository;
    private final EducatorRepository educatorRepository;
    private final SectionRepository sectionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        // Check if this is from an invitation to determine user type
        String userType = determineUserTypeFromInvitation(registerRequest.getEmail());
        
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .lastName(registerRequest.getLastName())
                .createdAt(Instant.now())
                .build();
        user = userRepository.save(user);
        
        FamilyGroup familyGroup = FamilyGroup.builder()
                .name(registerRequest.getLastName())
                .user(user)
                .createdAt(Instant.now())
                .build();
        familyGroup = familyGroupRepository.save(familyGroup);

        MemberType type = memberTypeRepository.findFirstByDescription("TUTOR");
        if (type == null) {throw new RuntimeException("Type not found");}

        Status status = statusRepository.getFirstByDescription("ACTIVE");
        if (status == null) {throw new RuntimeException("Status not found");}

        Member member = Member.builder()
                .dni(registerRequest.getTutor().getDni())
                .user(user)
                .familyGroup(familyGroup)
                .isTutor(true)
                .memberType(type)
                .contactPhone(registerRequest.getTutor().getContactPhone())
                .email(registerRequest.getEmail())
                .status(status)
                .name(registerRequest.getTutor().getName())
                .lastname(registerRequest.getTutor().getLastName())
                .address(registerRequest.getTutor().getAddress())
                .birthdate(registerRequest.getTutor().getBirthdate())
                .notes(registerRequest.getTutor().getNotes())
                .build();
        member = memberRepository.save(member);

        // Assign role based on user type
        String roleDescription = "EDUCATOR".equals(userType) ? "ROLE_EDUCATOR" : "ROLE_FAMILY";
        Role role = roleRepository.getFirstByDescription(roleDescription);
        if (role == null) {throw new RuntimeException("Role not found: " + roleDescription);}

        RolesXUser rolesXUser = RolesXUser.builder()
                .role(role)
                .user(user)
                .build();
        rolesXUser = rolesXUserRepository.save(rolesXUser);

        familyGroup.setMainContact(member);
        familyGroupRepository.save(familyGroup);
        
        // If user is an educator, create Educator entity
        if ("EDUCATOR".equals(userType)) {
            createEducatorEntity(user, registerRequest, userType);
        }
        
        // Update invitation status to COMPLETED
        updateInvitationStatusToCompleted(registerRequest.getEmail());
        
        return UserDto.builder()
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String determineUserTypeFromInvitation(String email) {
        Status pendingStatus = statusRepository.getFirstByDescription("PENDING");
        if (pendingStatus != null) {
            List<Invitation> invitations = invitationRepository.findByStatus(pendingStatus);
            for (Invitation invitation : invitations) {
                if (invitation.getEmail().equals(email)) {
                    return invitation.getUserType();
                }
            }
        }
        return "FAMILY"; // Default to FAMILY if no invitation found
    }

    private void createEducatorEntity(User user, RegisterRequest registerRequest, String userType) {
        // Find the section from the invitation
        Section section = getSectionFromInvitation(registerRequest.getEmail());
        
        Status activeStatus = statusRepository.getFirstByDescription("ACTIVE");
        if (activeStatus == null) {
            throw new RuntimeException("ACTIVE status not found");
        }

        Educator educator = Educator.builder()
                .user(user)
                .name(registerRequest.getTutor().getName())
                .lastname(registerRequest.getTutor().getLastName())
                .birthdate(registerRequest.getTutor().getBirthdate())
                .dni(registerRequest.getTutor().getDni())
                .address(registerRequest.getTutor().getAddress())
                .section(section)
                .startDate(LocalDate.now())
                .status(activeStatus)
                .notes(registerRequest.getTutor().getNotes())
                .accountBalance(BigDecimal.ZERO)
                .build();
        
        educatorRepository.save(educator);
    }

    private Section getSectionFromInvitation(String email) {
        Status pendingStatus = statusRepository.getFirstByDescription("PENDING");
        if (pendingStatus != null) {
            List<Invitation> invitations = invitationRepository.findByStatus(pendingStatus);
            for (Invitation invitation : invitations) {
                if (invitation.getEmail().equals(email)) {
                    return invitation.getSection();
                }
            }
        }
        return null;
    }

    private void updateInvitationStatusToCompleted(String email) {
        Status pendingStatus = statusRepository.getFirstByDescription("PENDING");
        Status completedStatus = statusRepository.getFirstByDescription("COMPLETED");
        
        if (pendingStatus != null && completedStatus != null) {
            List<Invitation> invitations = invitationRepository.findByStatus(pendingStatus);
            for (Invitation invitation : invitations) {
                if (invitation.getEmail().equals(email)) {
                    invitation.setStatus(completedStatus);
                    invitationRepository.save(invitation);
                    break;
                }
            }
        }
    }

    public NewUserDto inviteUser(NewUserDto newUserDto) {
        String token = jwtService.generateInvitationToken(newUserDto);
        Status status = statusRepository.getFirstByDescription("PENDING");
        if (status == null){
            throw new RuntimeException("Status not found");
        }
        
        // Validate userType
        if (!newUserDto.getUserType().equals("FAMILY") && !newUserDto.getUserType().equals("EDUCATOR")) {
            throw new RuntimeException("Invalid user type. Must be FAMILY or EDUCATOR");
        }
        
        // Validate section for educators
        Section section = null;
        if ("EDUCATOR".equals(newUserDto.getUserType())) {
            if (newUserDto.getSectionId() == null) {
                throw new RuntimeException("Section is required for educators");
            }
            section = sectionRepository.findById(newUserDto.getSectionId())
                    .orElseThrow(() -> new RuntimeException("Section not found"));
        }
        
        emailService.sendInvitation(newUserDto.getEmail(), newUserDto.getLastName(), newUserDto.getUserType(), token);
        Invitation invitation = Invitation.builder()
                .email(newUserDto.getEmail())
                .lastName(newUserDto.getLastName())
                .userType(newUserDto.getUserType())
                .section(section)
                .sendDate(LocalDateTime.now())
                .status(status)
                .build();
        invitationRepository.save(invitation);
        return newUserDto;
    }

    public UserDto getUserByEmail(String email) {
        User user = this.userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No user found"));
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .lastName(user.getLastName())
                .build();
    }

    public List<InvitationDto> getPendingInvitations() {
        Status pendingStatus = statusRepository.getFirstByDescription("PENDING");
        if (pendingStatus == null) {
            throw new RuntimeException("PENDING status not found");
        }
        
        List<Invitation> invitations = invitationRepository.findByStatus(pendingStatus);
        return invitations.stream()
                .map(this::mapToInvitationDto)
                .collect(Collectors.toList());
    }

    public List<InvitationDto> getCompletedRegistrations() {
        Status completedStatus = statusRepository.getFirstByDescription("COMPLETED");
        if (completedStatus == null) {
            throw new RuntimeException("COMPLETED status not found");
        }
        
        List<Invitation> invitations = invitationRepository.findByStatus(completedStatus);
        return invitations.stream()
                .map(this::mapToInvitationDto)
                .collect(Collectors.toList());
    }

    public InvitationDto resendInvitation(Integer invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        NewUserDto newUserDto = NewUserDto.builder()
                .email(invitation.getEmail())
                .lastName(invitation.getLastName())
                .userType(invitation.getUserType())
                .sectionId(invitation.getSection() != null ? invitation.getSection().getId() : null)
                .build();
        
        String token = jwtService.generateInvitationToken(newUserDto);
        emailService.sendInvitation(invitation.getEmail(), invitation.getLastName(), invitation.getUserType(), token);
        
        invitation.setSendDate(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        return mapToInvitationDto(invitation);
    }

    public void deleteInvitation(Integer invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        invitationRepository.delete(invitation);
    }

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    private InvitationDto mapToInvitationDto(Invitation invitation) {
        return InvitationDto.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .lastName(invitation.getLastName())
                .userType(invitation.getUserType())
                .status(invitation.getStatus().getDescription())
                .sentDate(invitation.getSendDate())
                .sectionName(invitation.getSection() != null ? invitation.getSection().getDescription() : null)
                .build();
    }
}
