package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.FamilyGroupDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.MemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.RelationshipDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.TutorDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FamilyGroupService {
    private final FamilyGroupRepository familyGroupRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final SectionRepository sectionRepository;
    private final RelationshipRepository relationshipRepository;
    private final MemberTypeRepository memberTypeRepository;
    private final StatusRepository statusRepository;
    private final MemberService memberService;

    @Transactional
    public FamilyGroup newFamilyGroup(TutorDto tutor){
        FamilyGroup familyGroup = new FamilyGroup();

        User user = userRepository.findById(tutor.getUserId())
                .orElseThrow(()-> new RuntimeException("User not found"));
        familyGroup.setUser(user);

        familyGroup.setName(tutor.getLastName());

        familyGroup.setCreatedAt(Instant.now());

        familyGroup = familyGroupRepository.save(familyGroup);

        //Todo actualizar cuando esten cargadas las tablas auxiliares bd
        Member mainContact = new Member();
        mainContact.setFamilyGroup(familyGroup);
        mainContact.setUser(user);
        mainContact.setAddress(tutor.getAddress());
        mainContact.setContactPhone(tutor.getContactPhone());
        mainContact.setEmail(tutor.getEmail());
        mainContact.setDni(mainContact.getDni());
        mainContact.setBirthdate(mainContact.getBirthdate());
        mainContact.setName(tutor.getName());
        mainContact.setLastname(tutor.getLastName());
        mainContact.setNotes(tutor.getNotes());
        mainContact.setIsTutor(true);

        mainContact = memberRepository.save(mainContact);

        familyGroup.setMainContact(mainContact);
        return familyGroupRepository.save(familyGroup);
    }

    @Transactional
    public TutorDto addTutorToFamilyGroup(TutorDto tutor){
        FamilyGroup familyGroup = familyGroupRepository.findFamilyGroupsByUser_Id(tutor.getUserId());
        if (familyGroup == null) {
            throw new RuntimeException("User not found");
        }
        Status status = statusRepository.getFirstByDescription("ACTIVE");
        MemberType type = memberTypeRepository.findFirstByDescription("TUTOR");

        Member tutorContact = getTutorContact(tutor, familyGroup);
        tutorContact.setStatus(status);
        tutorContact.setMemberType(type);
        tutorContact = memberRepository.save(tutorContact);

        familyGroup.getTutors().add(tutorContact);
        familyGroupRepository.save(familyGroup);

        return  toTutorDto(tutorContact);
    }
    @Transactional
    public MemberDto addMemberToFamilyGroup(MemberDto member){
        FamilyGroup familyGroup = familyGroupRepository.findFamilyGroupsByUser_Id(member.getUserId());
        if (familyGroup == null) {
            throw new RuntimeException("User not found");
        }
        Section section = sectionRepository.findFirstByDescription(member.getSection());
        if (section == null) {
            throw new RuntimeException("Section not found");
        }
        Status status = statusRepository.getFirstByDescription("ACTIVE");
        MemberType type = memberTypeRepository.findFirstByDescription("PROTAGONISTA");
        Member protagonist = new Member();
        protagonist.setFamilyGroup(familyGroup);
        protagonist.setAddress(member.getAddress());
        protagonist.setBirthdate(member.getBirthdate());
        protagonist.setDni(member.getDni());
        protagonist.setName(member.getName());
        protagonist.setLastname(member.getLastName());
        protagonist.setNotes(member.getNotes());
        protagonist.setIsTutor(false);
        protagonist.setSection(section);
        protagonist.setUser(familyGroup.getUser());
        protagonist.setStatus(status);
        protagonist.setMemberType(type);
        protagonist = memberRepository.save(protagonist);


        return toMemberDto(protagonist);
    }

    private static Member getTutorContact(TutorDto tutor, FamilyGroup familyGroup) {
        Member tutorContact = new Member();
        tutorContact.setFamilyGroup(familyGroup);
        tutorContact.setIsTutor(true);
        tutorContact.setContactPhone(tutor.getContactPhone());
        tutorContact.setAddress(tutor.getAddress());
        tutorContact.setEmail(tutor.getEmail());
        tutorContact.setDni(tutor.getDni());
        tutorContact.setName(tutor.getName());
        tutorContact.setLastname(tutor.getLastName());
        tutorContact.setNotes(tutor.getNotes());
        tutorContact.setUser(familyGroup.getUser());
        tutorContact.setBirthdate(tutor.getBirthdate());
        return tutorContact;
    }
//    @Transactional
//    public FamilyGroup updateTutor(TutorDto tutorDto){
//        Member tutor = memberRepository.findById(tutorDto.getId()).orElseThrow(()-> new RuntimeException("Tutor not found"));
//        tutor.setName(tutorDto.getName());
//        tutor.setLastname(tutorDto.getLastName());
//        tutor.setNotes(tutorDto.getNotes());
//        tutor.setDni(tutorDto.getDni());
//        tutor.setContactPhone(tutorDto.getContactPhone());
//        tutor.setEmail(tutorDto.getEmail());
//        tutor.setBirthdate(tutorDto.getBirthdate());
//        tutor = memberRepository.save(tutor);
//        return tutor.getFamilyGroup();
//    }
//    @Transactional
//    public  FamilyGroup updateMember(MemberDto memberDto){
//        Member member =memberRepository.findById(memberDto.getId()).orElseThrow(()-> new RuntimeException("Member not found"));
//        Section section = sectionRepository.findFirstByDescription(memberDto.getSection());
//        if (section == null) {throw new RuntimeException("Section not found");}
//        member.setSection(section);
//        member.setNotes(memberDto.getNotes());
//        member.setDni(memberDto.getDni());
//        member.setName(memberDto.getName());
//        member.setLastname(memberDto.getLastName());
//        member.setBirthdate(memberDto.getBirthdate());
//        member = memberRepository.save(member);
//        return member.getFamilyGroup();
//    }

    public FamilyGroupDto getFamilyGroup(Integer userId){
        FamilyGroup familyGroup = familyGroupRepository.findFamilyGroupsByUser_Id(userId);
        if (familyGroup == null) {throw new RuntimeException("User not found");}
        List<TutorDto> tutors = new ArrayList<>();
        List<MemberDto> members = new ArrayList<>();
        for (Member tutor : familyGroup.getTutors()) {
            tutors.add(toTutorDto(tutor));
        }
        for (Member member : familyGroup.getMemberProtagonists()) {
            members.add(toMemberDto(member));
        }
         FamilyGroupDto x= FamilyGroupDto
                .builder()
                .id(familyGroup.getId())
                .mainContact(toTutorDto(familyGroup.getMainContact()))
                .members(members)
                .tutors(tutors)
                .build();

        return x;
    }

    private TutorDto toTutorDto(Member member){
        if (!member.getIsTutor()){throw new RuntimeException("Member is not a tutor");}
        List<Relationship> relationships = relationshipRepository.findAllByTutor_Id(member.getId());
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
        return TutorDto.builder()
                .id(member.getId())
                .name(member.getName())
                .lastName(member.getLastname())
                .notes(member.getNotes())
                .dni(member.getDni())
                .address(member.getAddress())
                .birthdate(member.getBirthdate())
                .contactPhone(member.getContactPhone())
                .email(member.getEmail())
                .userId(member.getUser().getId())
                .relationships(relations)
                .build();
    }

    private MemberDto toMemberDto(Member member){
        if (member.getIsTutor()){throw new RuntimeException("Member is a tutor");}
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
        return MemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .lastName(member.getLastname())
                .address(member.getAddress())
                .notes(member.getNotes())
                .dni(member.getDni())
                .accountBalance(member.getAccountBalance())
                .birthdate(member.getBirthdate())
                .memberType("Protagonista")
                .relationships(relations)
                .section(member.getSection().getDescription())
                .userId(member.getUser().getId())
                .build();
    }

    public RelationshipDto addRelationship(RelationshipDto relationshipDto){
        Member tutor = memberRepository.findById(relationshipDto.getTutorId())
                .orElseThrow(()-> new RuntimeException("Member not found"));
        Member protagonist = memberRepository.findById(relationshipDto.getMemberId())
                .orElseThrow(()-> new RuntimeException("Member not found"));
        Relationship relationship = new Relationship();
        relationship.setTutor(tutor);
        relationship.setMember(protagonist);
        relationship.setRelationship(relationshipDto.getRelationship());
        relationship = relationshipRepository.save(relationship);
        return relationshipDto;
    }

    public TutorDto updateTutor(TutorDto tutorDto, Integer userId){
        return toTutorDto(memberService.updateMember(tutorDto, userId));
    }

    public MemberDto updateMember(MemberDto memberDto, Integer userId){
        return toMemberDto(memberService.updateMember(memberDto, userId));
    }

    public RelationshipDto updateRelationship(RelationshipDto relationshipDto, Integer id){
        if (!Objects.equals(relationshipDto.getId(), id)){throw new RuntimeException("ids do not match");}
        Relationship relationship = relationshipRepository.findById(id).orElseThrow(()-> new RuntimeException("Relationship not found"));
        relationship.setRelationship(relationshipDto.getRelationship());
        relationshipRepository.save(relationship);
        return relationshipDto;
    }
}
