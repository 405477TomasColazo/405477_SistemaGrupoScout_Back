package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.MemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.RelationshipDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.SectionMemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.TutorDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.exceptions.DuplicateDniException;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SectionRepository sectionRepository;
    private final EducatorRepository educatorRepository;
    private final RelationshipRepository relationshipRepository;
    private final MemberTypeRepository memberTypeRepository;
    private final StatusRepository statusRepository;
    private final MonthlyFeeService monthlyFeeService;


    public Member updateMember(TutorDto tutorDto,Integer id) {
        if (!Objects.equals(id, tutorDto.getId())) {throw new RuntimeException("id mismatch");}
        Member member = memberRepository.findById(id).orElseThrow(()->new RuntimeException("Member not found"));
        
        // Check if member is active
        if (!member.getIsActive()) {
            throw new RuntimeException("Cannot update inactive member");
        }
        
        member.setContactPhone(tutorDto.getContactPhone());
        member.setEmail(tutorDto.getEmail());
        member.setName(tutorDto.getName());
        member.setLastname(tutorDto.getLastName());
        member.setNotes(tutorDto.getNotes());
        member.setAddress(tutorDto.getAddress());
        member.setBirthdate(tutorDto.getBirthdate());
        member.setDni(tutorDto.getDni());
        return memberRepository.save(member);
    }

    public Member updateMember(MemberDto memberDto, Integer id) {
        if (!Objects.equals(id, memberDto.getId())) {throw new RuntimeException("id mismatch");}
        Member member = memberRepository.findById(id).orElseThrow(()->new RuntimeException("Member not found"));
        
        // Check if member is active
        if (!member.getIsActive()) {
            throw new RuntimeException("Cannot update inactive member");
        }
        
        member.setName(memberDto.getName());
        member.setLastname(memberDto.getLastName());
        member.setAddress(memberDto.getAddress());
        member.setNotes(memberDto.getNotes());
        member.setBirthdate(memberDto.getBirthdate());
        member.setDni(memberDto.getDni());
        
        // Auto-assign section if birthdate changed and member is not a tutor
        if (!member.getIsTutor()) {
            Section newSection = calculateSectionByAge(memberDto.getBirthdate());
            if (newSection != null) {
                member.setSection(newSection);
            }
        }
        
        return memberRepository.save(member);
    }

    public List<SectionMemberDto> getAllMembers() {
        // Use new method that filters active members
        return getAllActiveMembers();
    }

    public List<SectionMemberDto> getMembersByEducator(Integer userId) {
        // Use new method that filters active members
        return getActiveMembersByEducator(userId);
    }

    private SectionMemberDto toSectionMemberDto(Member member) {
        List<Relationship> relationships = relationshipRepository.findAllByMember_Id(member.getId());
        List<RelationshipDto> relations = new ArrayList<>();
        for (Relationship relationship : relationships) {
            RelationshipDto r = RelationshipDto.builder()
                    .id(relationship.getId())
                    .tutorId(relationship.getTutor().getId())
                    .memberId(relationship.getMember().getId())
                    .relationship(relationship.getRelationship())
                    .build();
            
            relations.add(r);
        }

        return SectionMemberDto.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .name(member.getName())
                .lastName(member.getLastname())
                .dni(member.getDni())
                .section(member.getSection().getDescription())
                .birthdate(member.getBirthdate())
                .notes(member.getNotes())
                .relationships(relations)
                .accountBalance(member.getAccountBalance())
                .isEducator(false)
                .address(member.getAddress())
                .build();
    }

    private SectionMemberDto toSectionMemberDto(Educator educator) {
        return SectionMemberDto.builder()
                .id(educator.getId())
                .userId(educator.getUser().getId())
                .name(educator.getName())
                .lastName(educator.getLastname())
                .dni(educator.getDni())
                .section(educator.getSection().getDescription())
                .birthdate(educator.getBirthdate())
                .notes(educator.getNotes())
                .accountBalance(educator.getAccountBalance())
                .isEducator(true)
                .address(educator.getAddress())
                .relationships(new ArrayList<>())
                .build();
    }

    // Age calculation and section assignment
    public int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    public Section calculateSectionByAge(LocalDate birthdate) {
        int age = calculateAge(birthdate);
        Optional<Section> section = sectionRepository.findByAgeRange(age);
        
        // If no section found for age, assign a default section or handle gracefully
        if (section.isEmpty()) {
            // You can either:
            // 1. Return a default section
            // 2. Allow null (and handle it in the entity)
            // 3. Throw an exception with guidance
            
            // For now, let's allow null and log a warning
            System.out.println("Warning: No section found for age " + age + ". Member will be created without section.");
            return null;
        }
        
        return section.get();
    }

    // DNI validation methods
    public void validateDniForCreation(String dni, User user) throws DuplicateDniException {
        Optional<Member> existingMember = memberRepository.findByDniAndFamilyGroup_User(dni, user);
        
        if (existingMember.isPresent()) {
            Member member = existingMember.get();
            if (member.getIsActive()) {
                throw new DuplicateDniException("Ya existe un miembro activo con el DNI " + dni, member.getId());
            }
            // If member exists but is inactive, it can be reactivated
            throw new DuplicateDniException("Existe un miembro inactivo con el DNI " + dni + ". ¿Desea reactivarlo?", member.getId());
        }
    }

    // Soft delete methods
    public void softDeleteMember(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        member.setIsActive(false);
        member.setDeletedAt(LocalDateTime.now());
        memberRepository.save(member);
    }

    public Member reactivateMember(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        member.setIsActive(true);
        member.setDeletedAt(null);
        Member savedMember = memberRepository.save(member);
        
        // Generate monthly fee for reactivated protagonista member
        try {
            monthlyFeeService.generateFeesForNewMember(savedMember);
        } catch (Exception e) {
            // Log error but don't fail member reactivation
            System.err.println("Error generating monthly fees for reactivated member: " + e.getMessage());
        }
        
        return savedMember;
    }

    // Helper methods to get default entities
    private MemberType getDefaultMemberType() {
        return memberTypeRepository.findById(1) // Assuming ID 1 is the default type
                .orElseThrow(() -> new RuntimeException("Default member type not found"));
    }
    
    private Status getActiveStatus() {
        return statusRepository.findById(1) // Assuming ID 1 is the active status
                .orElseThrow(() -> new RuntimeException("Active status not found"));
    }

    // Create methods with validations
    public Member createMember(MemberDto memberDto, FamilyGroup familyGroup) throws DuplicateDniException {
        // Validate DNI
        validateDniForCreation(memberDto.getDni(), familyGroup.getUser());
        
        // Auto-assign section based on age
        Section section = calculateSectionByAge(memberDto.getBirthdate());
        
        Member member = Member.builder()
                .name(memberDto.getName())
                .lastname(memberDto.getLastName())
                .address(memberDto.getAddress())
                .birthdate(memberDto.getBirthdate())
                .dni(memberDto.getDni())
                .notes(memberDto.getNotes())
                .email(memberDto.getEmail()) // Optional
                .contactPhone(memberDto.getContactPhone()) // Optional
                .familyGroup(familyGroup)
                .section(section)
                .memberType(getDefaultMemberType())
                .status(getActiveStatus())
                .isTutor(false)
                .isActive(true)
                .accountBalance(java.math.BigDecimal.ZERO)
                .build();
                
        Member savedMember = memberRepository.save(member);
        
        // Generate monthly fee for new protagonista member
        try {
            monthlyFeeService.generateFeesForNewMember(savedMember);
        } catch (Exception e) {
            // Log error but don't fail member creation
            System.err.println("Error generating monthly fees for new member: " + e.getMessage());
        }
        
        return savedMember;
    }

    public Member createTutor(TutorDto tutorDto, FamilyGroup familyGroup) throws DuplicateDniException {
        // Validate DNI
        validateDniForCreation(tutorDto.getDni(), familyGroup.getUser());
        
        Member tutor = Member.builder()
                .name(tutorDto.getName())
                .lastname(tutorDto.getLastName())
                .address(tutorDto.getAddress())
                .birthdate(tutorDto.getBirthdate())
                .dni(tutorDto.getDni())
                .notes(tutorDto.getNotes())
                .email(tutorDto.getEmail())
                .contactPhone(tutorDto.getContactPhone())
                .familyGroup(familyGroup)
                .memberType(getDefaultMemberType())
                .status(getActiveStatus())
                .isTutor(true)
                .isActive(true)
                .accountBalance(java.math.BigDecimal.ZERO)
                .build();
                
        return memberRepository.save(tutor);
    }

    // Updated queries to use soft delete
    public List<SectionMemberDto> getAllActiveMembers() {
        List<Member> members = memberRepository.findAllByIsActiveTrueAndIsTutor(false);
        List<Educator> educators = educatorRepository.findAll();
        List<SectionMemberDto> sectionMemberDtos = new ArrayList<>();
        
        members.forEach(member -> {
            sectionMemberDtos.add(toSectionMemberDto(member));
        });
        educators.forEach(educator -> {
            sectionMemberDtos.add(toSectionMemberDto(educator));
        });
        return sectionMemberDtos;
    }

    public List<SectionMemberDto> getActiveMembersByEducator(Integer userId) {
        Educator educator = educatorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Educator not found"));
        List<Member> members = memberRepository.findAllByIsActiveTrueAndSection(educator.getSection());
        List<Educator> educators = educatorRepository.findAllBySection(educator.getSection());
        List<SectionMemberDto> sectionMemberDtos = new ArrayList<>();
        
        for (Member member : members) {
            sectionMemberDtos.add(toSectionMemberDto(member));
        }
        for (Educator edu : educators) {
            sectionMemberDtos.add(toSectionMemberDto(edu));
        }
        return sectionMemberDtos;
    }
}
