package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.MemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.TutorDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;


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
}
