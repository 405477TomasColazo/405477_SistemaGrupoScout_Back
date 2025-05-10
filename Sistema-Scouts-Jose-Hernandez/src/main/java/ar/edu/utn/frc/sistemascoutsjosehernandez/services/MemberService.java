package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.MemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.RelationshipDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.SectionMemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.TutorDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Educator;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Relationship;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SectionRepository sectionRepository;
    private final EducatorRepository educatorRepository;
    private final RelationshipRepository relationshipRepository;


    public Member updateMember(TutorDto tutorDto,Integer id) {
        if (!Objects.equals(id, tutorDto.getId())) {throw new RuntimeException("id mismatch");}
        Member member = memberRepository.findById(id).orElseThrow(()->new RuntimeException("Member not found"));
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
        member.setName(memberDto.getName());
        member.setLastname(memberDto.getLastName());
        member.setAddress(memberDto.getAddress());
        member.setNotes(memberDto.getNotes());
        member.setBirthdate(memberDto.getBirthdate());
        member.setDni(memberDto.getDni());
        return memberRepository.save(member);
    }

    public List<SectionMemberDto> getAllMembers() {
        List<Member> members = memberRepository.findAllByIsTutor(false);
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

    public List<SectionMemberDto> getMembersByEducator(Integer userId) {
        Educator educator = educatorRepository.findByUser_Id(userId)
                .orElseThrow(()->new RuntimeException("Educator not found"));
        List<Member> members = memberRepository.findAllBySection(educator.getSection());
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
}
