package ar.edu.utn.frc.sistemascoutsjosehernandez.services;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.MemberDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.TutorDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.FamilyGroup;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.FamilyGroupRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.SectionRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FamilyGroupService {
    private final FamilyGroupRepository familyGroupRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final SectionRepository sectionRepository;
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
    public FamilyGroup addTutorToFamilyGroup(TutorDto tutor){
        FamilyGroup familyGroup = familyGroupRepository.findFamilyGroupsByUser_Id(tutor.getUserId());
        if (familyGroup == null) {
            throw new RuntimeException("User not found");
        }
        Member tutorContact = getTutorContact(tutor, familyGroup);
        tutorContact = memberRepository.save(tutorContact);

        familyGroup.getTutors().add(tutorContact);
        return familyGroupRepository.save(familyGroup);
    }
    @Transactional
    public FamilyGroup addMemberToFamilyGroup(MemberDto member){
        FamilyGroup familyGroup = familyGroupRepository.findFamilyGroupsByUser_Id(member.getUserId());
        if (familyGroup == null) {
            throw new RuntimeException("User not found");
        }
        Section section = sectionRepository.findFirstByDescription(member.getSection());
        if (section == null) {
            throw new RuntimeException("Section not found");
        }
        Member protagonist = new Member();
        protagonist.setFamilyGroup(familyGroup);
        protagonist.setBirthdate(member.getBirthDate());
        protagonist.setDni(member.getDni());
        protagonist.setName(member.getName());
        protagonist.setLastname(member.getLastName());
        protagonist.setNotes(member.getNotes());
        protagonist.setIsTutor(false);
        protagonist.setSection(section);
        protagonist.setUser(familyGroup.getUser());
        protagonist = memberRepository.save(protagonist);

        familyGroup.getMembers().add(protagonist);
        return familyGroupRepository.save(familyGroup);
    }

    private static Member getTutorContact(TutorDto tutor, FamilyGroup familyGroup) {
        Member tutorContact = new Member();
        tutorContact.setFamilyGroup(familyGroup);
        tutorContact.setIsTutor(true);
        tutorContact.setContactPhone(tutor.getContactPhone());
        tutorContact.setEmail(tutor.getEmail());
        tutorContact.setDni(tutor.getDni());
        tutorContact.setName(tutor.getName());
        tutorContact.setLastname(tutor.getLastName());
        tutorContact.setNotes(tutor.getNotes());
        tutorContact.setUser(familyGroup.getUser());
        tutorContact.setBirthdate(tutor.getBirthDate());
        return tutorContact;
    }
}
