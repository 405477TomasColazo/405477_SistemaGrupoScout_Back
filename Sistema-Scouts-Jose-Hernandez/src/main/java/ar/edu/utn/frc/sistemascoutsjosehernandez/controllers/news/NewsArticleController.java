package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.*;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.news.NewsArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News Articles", description = "API para gestión de artículos de noticias")
@CrossOrigin(origins = "http://localhost:4200")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    @GetMapping
    @Operation(summary = "Obtener artículos publicados", description = "Lista paginada de artículos publicados")
    public ResponseEntity<Page<NewsArticleSummaryDto>> getPublishedArticles(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NewsArticleSummaryDto> articles = newsArticleService.getPublishedArticles(pageable);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/latest")
    @Operation(summary = "Obtener últimos artículos", description = "Últimos artículos para mostrar en la landing page")
    public ResponseEntity<List<NewsArticleSummaryDto>> getLatestArticles(
            @Parameter(description = "Número de artículos a obtener") 
            @RequestParam(defaultValue = "5") int limit) {
        List<NewsArticleSummaryDto> articles = newsArticleService.getLatestArticles(limit);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Obtener artículo por slug", description = "Obtiene un artículo específico por su slug")
    public ResponseEntity<NewsArticleDto> getArticleBySlug(
            @Parameter(description = "Slug del artículo") 
            @PathVariable String slug) {
        
        newsArticleService.incrementViews(slug);
        
        return newsArticleService.getArticleBySlug(slug)
                .map(article -> ResponseEntity.ok(article))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar artículos", description = "Búsqueda de texto completo en artículos publicados")
    public ResponseEntity<Page<NewsArticleSummaryDto>> searchArticles(
            @Parameter(description = "Término de búsqueda") 
            @RequestParam String q,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NewsArticleSummaryDto> articles = newsArticleService.searchPublishedArticles(q, pageable);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Obtener artículos por categoría", description = "Lista artículos de una categoría específica")
    public ResponseEntity<Page<NewsArticleSummaryDto>> getArticlesByCategory(
            @Parameter(description = "ID de la categoría") 
            @PathVariable Integer categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NewsArticleSummaryDto> articles = newsArticleService.getArticlesByCategory(categoryId, pageable);
        return ResponseEntity.ok(articles);
    }

    // Admin endpoints
    @GetMapping("/admin")
    @Operation(summary = "Obtener todos los artículos (Admin)", description = "Lista todos los artículos para administradores")
    public ResponseEntity<Page<NewsArticleSummaryDto>> getAllArticlesAdmin(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NewsArticleSummaryDto> articles = newsArticleService.getAllArticlesAdmin(pageable);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/admin/{id}")
    @Operation(summary = "Obtener artículo por ID (Admin)", description = "Obtiene un artículo específico para administradores")
    public ResponseEntity<NewsArticleDto> getArticleByIdAdmin(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer id) {
        return newsArticleService.getArticleByIdAdmin(id)
                .map(article -> ResponseEntity.ok(article))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear artículo", description = "Crea un nuevo artículo de noticia")
    public ResponseEntity<NewsArticleDto> createArticle(
            @Valid @RequestBody CreateNewsArticleDto createDto) {
        NewsArticleDto createdArticle = newsArticleService.createArticle(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar artículo", description = "Actualiza un artículo existente")
    public ResponseEntity<NewsArticleDto> updateArticle(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer id,
            @Valid @RequestBody UpdateNewsArticleDto updateDto) {
        try {
            NewsArticleDto updatedArticle = newsArticleService.updateArticle(id, updateDto);
            return ResponseEntity.ok(updatedArticle);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publicar artículo", description = "Cambia el estado del artículo a publicado")
    public ResponseEntity<NewsArticleDto> publishArticle(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer id) {
        try {
            NewsArticleDto publishedArticle = newsArticleService.publishArticle(id);
            return ResponseEntity.ok(publishedArticle);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Despublicar artículo", description = "Cambia el estado del artículo a borrador")
    public ResponseEntity<NewsArticleDto> unpublishArticle(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer id) {
        try {
            NewsArticleDto unpublishedArticle = newsArticleService.unpublishArticle(id);
            return ResponseEntity.ok(unpublishedArticle);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar artículo", description = "Elimina un artículo permanentemente")
    public ResponseEntity<Void> deleteArticle(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer id) {
        try {
            newsArticleService.deleteArticle(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}