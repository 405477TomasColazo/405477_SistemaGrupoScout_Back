package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.UserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.NewUserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.InvitationDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.RegisterRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.UpdateProfileRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.ChangePasswordRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.ForgotPasswordRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.ResetPasswordRequest;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
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
                .isActive(true)
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
                .avatar(user.getAvatar())
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


    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // Password Reset Methods

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        Instant tokenExpiry = Instant.now().plus(1, ChronoUnit.HOURS); // Token expires in 1 hour

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(tokenExpiry);
        userRepository.save(user);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getLastName(), resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is expired
        if (user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Check if new password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Update password and clear reset token
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    public boolean validateResetToken(String token) {
        return userRepository.findByPasswordResetToken(token)
                .map(user -> user.getPasswordResetTokenExpiry().isAfter(Instant.now()))
                .orElse(false);
    }

    // Avatar management
    private String getAvatarUrl(String avatarId) {
        if (avatarId == null || avatarId.trim().isEmpty()) {
            avatarId = "default";
        }
        return "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=" + avatarId;
    }

    public List<AvatarOption> getAvailableAvatars() {
        return List.of(
            new AvatarOption("default", "Por defecto", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=default"),
            new AvatarOption("scout", "Scout", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=scout"),
            new AvatarOption("explorer", "Explorador", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=explorer"),
            new AvatarOption("ranger", "Guardabosque", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=ranger"),
            new AvatarOption("adventure", "Aventurero", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=adventure"),
            new AvatarOption("nature", "Naturaleza", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=nature"),
            new AvatarOption("forest", "Bosque", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=forest"),
            new AvatarOption("mountain", "Montaña", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=mountain"),
            new AvatarOption("river", "Río", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=river"),
            new AvatarOption("campfire", "Fogón", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=campfire"),
            new AvatarOption("compass", "Brújula", "https://api.dicebear.com/9.x/lorelei-neutral/svg?seed=compass")
        );
    }

    // Update user profile and return UserDto
    public UserDto updateUserProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists");
            }
        }

        // Update user fields
        user.setEmail(request.getEmail());
        user.setLastName(request.getLastName());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        User savedUser = userRepository.save(user);

        return UserDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .lastName(savedUser.getLastName())
                .avatar(savedUser.getAvatar())
                .createdAt(savedUser.getCreatedAt())
                .lastLogin(savedUser.getLastLogin())
                .build();
    }

    // Helper class for avatar options
    public static class AvatarOption {
        private String id;
        private String name;
        private String url;

        public AvatarOption(String id, String name, String url) {
            this.id = id;
            this.name = name;
            this.url = url;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUrl() { return url; }
    }
}
