package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.CreateDistributionDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.NewsDistributionDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.User;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.DistributionFilter;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsDistribution;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.news.NewsDistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News Distribution", description = "API para distribución de noticias por email")
@CrossOrigin(origins = "http://localhost:4200")
public class NewsDistributionController {

    private final NewsDistributionService newsDistributionService;

    @PostMapping("/{articleId}/distribute")
    @PreAuthorize("hasAnyRole('NEWS_MANAGER', 'ADMIN')")
    @Operation(summary = "Crear distribución", description = "Crea una nueva distribución de noticia por email")
    public ResponseEntity<NewsDistributionDto> createDistribution(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer articleId,
            @Valid @RequestBody CreateDistributionDto createDto) {
        try {
            List<DistributionFilter.FilterType> filterTypes = createDto.getFilters().stream()
                    .map(CreateDistributionDto.FilterDto::getFilterType)
                    .collect(Collectors.toList());
            
            List<String> filterValues = createDto.getFilters().stream()
                    .map(CreateDistributionDto.FilterDto::getFilterValue)
                    .collect(Collectors.toList());

            NewsDistribution distribution = newsDistributionService.createDistribution(
                    articleId, filterTypes, filterValues);
            
            NewsDistributionDto distributionDto = convertToDto(distribution);
            return ResponseEntity.status(HttpStatus.CREATED).body(distributionDto);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/distributions/{distributionId}/send")
    @PreAuthorize("hasAnyRole('NEWS_MANAGER', 'ADMIN')")
    @Operation(summary = "Enviar distribución", description = "Ejecuta el envío de emails de una distribución")
    public ResponseEntity<Void> sendDistribution(
            @Parameter(description = "ID de la distribución") 
            @PathVariable Integer distributionId) {
        try {
            newsDistributionService.sendDistribution(distributionId);
            return ResponseEntity.accepted().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{articleId}/distributions")
    @PreAuthorize("hasAnyRole('NEWS_MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener historial de distribuciones", description = "Lista el historial de distribuciones de un artículo")
    public ResponseEntity<List<NewsDistributionDto>> getDistributionHistory(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer articleId) {
        List<NewsDistribution> distributions = newsDistributionService.getDistributionHistory(articleId);
        List<NewsDistributionDto> distributionDtos = distributions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(distributionDtos);
    }

    @GetMapping("/distributions/{distributionId}/recipients/preview")
    @PreAuthorize("hasAnyRole('NEWS_MANAGER', 'ADMIN')")
    @Operation(summary = "Vista previa de destinatarios", description = "Muestra los destinatarios de una distribución")
    public ResponseEntity<List<String>> previewRecipients(
            @Parameter(description = "ID de la distribución") 
            @PathVariable Integer distributionId) {
        try {
            // Para implementar: obtener la distribución y mostrar preview de destinatarios
            // List<User> recipients = newsDistributionService.getRecipients(distribution.getFilters());
            // List<String> emails = recipients.stream().map(User::getEmail).collect(Collectors.toList());
            return ResponseEntity.ok(List.of("ejemplo@email.com"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private NewsDistributionDto convertToDto(NewsDistribution distribution) {
        return NewsDistributionDto.builder()
                .id(distribution.getId())
                .articleId(distribution.getArticle().getId())
                .articleTitle(distribution.getArticle().getTitle())
                .sentAt(distribution.getSentAt())
                .sentByName( distribution.getSentBy().getLastName())
                .totalRecipients(distribution.getTotalRecipients())
                .status(distribution.getStatus())
                .filters(distribution.getFilters().stream()
                        .map(filter -> NewsDistributionDto.DistributionFilterDto.builder()
                                .filterType(filter.getFilterType().toString())
                                .filterValue(filter.getFilterValue())
                                .filterDescription(getFilterDescription(filter))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private String getFilterDescription(DistributionFilter filter) {
        switch (filter.getFilterType()) {
            case ALL:
                return "Todos los usuarios";
            case SECTION:
                return "Sección: " + filter.getFilterValue();
            case MEMBER_TYPE:
                return "Tipo de miembro: " + filter.getFilterValue();
            case FAMILY_GROUP:
                return "Grupo familiar: " + filter.getFilterValue();
            default:
                return filter.getFilterType().toString();
        }
    }
}