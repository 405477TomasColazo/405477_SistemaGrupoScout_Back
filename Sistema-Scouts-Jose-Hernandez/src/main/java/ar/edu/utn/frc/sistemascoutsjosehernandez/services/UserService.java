package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.UserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.NewUserDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.auth.RegisterRequest;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

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

    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(registerRequest.getPassword())
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

        Role role = roleRepository.getFirstByDescription("ROLE_USER");
        if (role == null) {throw new RuntimeException("Role not found");}

        RolesXUser rolesXUser = RolesXUser.builder()
                .role(role)
                .user(user)
                .build();
        rolesXUser = rolesXUserRepository.save(rolesXUser);

        familyGroup.setMainContact(member);
        familyGroupRepository.save(familyGroup);
        return UserDto.builder()
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public NewUserDto inviteUser(NewUserDto newUserDto) {
        String token = jwtService.generateInvitationToken(newUserDto.getEmail());
        Status status = statusRepository.getFirstByDescription("PENDING");
        if (status == null){
            throw new RuntimeException("Status not found");
        }
        emailService.sendInvitation(newUserDto.getEmail(), token);
        Invitation invitation = Invitation.builder()
                .email(newUserDto.getEmail())
                .lastName(newUserDto.getLastName())
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
}
