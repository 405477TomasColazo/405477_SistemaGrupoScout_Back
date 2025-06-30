package ar.edu.utn.frc.sistemascoutsjosehernandez.services.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Member;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.Section;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.DistributionFilter;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsDistribution;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.MemberRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.SectionRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.UserRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsArticleRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsDistributionRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.EmailService;
import ar.edu.utn.frc.sistemascoutsjosehernandez.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsDistributionService {

    private final NewsDistributionRepository newsDistributionRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final SectionRepository sectionRepository;

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsDistribution createDistribution(Integer articleId, List<DistributionFilter.FilterType> filterTypes, List<String> filterValues) {
        NewsArticle article = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        if (article.getStatus() != NewsArticle.NewsStatus.PUBLISHED) {
            throw new RuntimeException("Solo se pueden distribuir artículos publicados");
        }

        User currentUser = SecurityUtils.getCurrentUser();
        
        NewsDistribution distribution = NewsDistribution.builder()
                .article(article)
                .sentBy(currentUser)
                .status(NewsDistribution.DistributionStatus.PENDING)
                .build();

        Set<DistributionFilter> filters = new HashSet<>();
        for (int i = 0; i < filterTypes.size(); i++) {
            DistributionFilter filter = DistributionFilter.builder()
                    .distribution(distribution)
                    .filterType(filterTypes.get(i))
                    .filterValue(i < filterValues.size() ? filterValues.get(i) : null)
                    .build();
            filters.add(filter);
        }
        
        distribution.setFilters(filters);
        
        List<User> recipients = getRecipients(filters);
        distribution.setTotalRecipients(recipients.size());

        return newsDistributionRepository.save(distribution);
    }

    @Async
    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public void sendDistribution(Integer distributionId) {
        NewsDistribution distribution = newsDistributionRepository.findById(distributionId)
                .orElseThrow(() -> new RuntimeException("Distribución no encontrada"));

        try {
            distribution.setStatus(NewsDistribution.DistributionStatus.SENDING);
            newsDistributionRepository.save(distribution);

            List<User> recipients = getRecipients(distribution.getFilters());
            
            emailService.sendBulkNewsNotification(recipients, distribution.getArticle());
            
            distribution.setStatus(NewsDistribution.DistributionStatus.SENT);
            distribution.setSentAt(LocalDateTime.now());
            
        } catch (Exception e) {
            distribution.setStatus(NewsDistribution.DistributionStatus.FAILED);
            throw new RuntimeException("Error enviando distribución: " + e.getMessage());
        } finally {
            newsDistributionRepository.save(distribution);
        }
    }

    @Transactional(readOnly = true)
    public List<NewsDistribution> getDistributionHistory(Integer articleId) {
        return newsDistributionRepository.findByArticleIdOrderBySentAtDesc(articleId);
    }

    @Transactional(readOnly = true)
    public List<User> getRecipients(Set<DistributionFilter> filters) {
        Set<User> recipients = new HashSet<>();

        for (DistributionFilter filter : filters) {
            switch (filter.getFilterType()) {
                case ALL:
                    recipients.addAll(userRepository.findAll());
                    break;
                    
                case SECTION:
                    Integer sectionId = Integer.valueOf(filter.getFilterValue());
                    Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new RuntimeException("Section not found"));
                    List<Member> membersInSection = memberRepository.findAllBySection(section);
                    recipients.addAll(membersInSection.stream()
                            .map(Member::getUser)
                            .filter(user -> user != null)
                            .collect(Collectors.toSet()));
                    
                    recipients.addAll(membersInSection.stream()
                            .flatMap(member -> member.getFamilyGroup().getMembers().stream())
                            .map(Member::getUser)
                            .filter(user -> user != null)
                            .collect(Collectors.toSet()));
                    break;
                    
                case MEMBER_TYPE:
                    Integer memberTypeId = Integer.valueOf(filter.getFilterValue());
                    List<Member> membersOfType = memberRepository.findAllByMemberType_Id(memberTypeId);
                    recipients.addAll(membersOfType.stream()
                            .map(Member::getUser)
                            .filter(user -> user != null)
                            .collect(Collectors.toSet()));
                    
                    recipients.addAll(membersOfType.stream()
                            .flatMap(member -> member.getFamilyGroup().getMembers().stream())
                            .map(Member::getUser)
                            .filter(user -> user != null)
                            .collect(Collectors.toSet()));
                    break;
                    
                case FAMILY_GROUP:
                    Integer familyGroupId = Integer.valueOf(filter.getFilterValue());
                    List<Member> familyMembers = memberRepository.findAllByFamilyGroup_Id(familyGroupId);
                    recipients.addAll(familyMembers.stream()
                            .map(Member::getUser)
                            .filter(user -> user != null)
                            .collect(Collectors.toSet()));
                    break;
            }
        }

        return recipients.stream()
                .filter(user -> user.getEmail() != null && !user.getEmail().isEmpty())
                .collect(Collectors.toList());
    }
}