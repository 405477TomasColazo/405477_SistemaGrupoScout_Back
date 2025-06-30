package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsImage;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.news.NewsImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News Images", description = "API para gestión de imágenes de noticias")
@CrossOrigin(origins = "http://localhost:4200")
public class NewsImageController {

    private final NewsImageService newsImageService;

    @PostMapping("/{articleId}/images")
    @Operation(summary = "Subir imagen", description = "Sube una imagen para un artículo específico")
    public ResponseEntity<NewsImage> uploadImage(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer articleId,
            @Parameter(description = "Archivo de imagen") 
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Texto alternativo de la imagen") 
            @RequestParam(required = false) String altText,
            @Parameter(description = "Descripción de la imagen") 
            @RequestParam(required = false) String caption) {
        try {
            NewsImage uploadedImage = newsImageService.uploadImage(articleId, file, altText, caption);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{articleId}/images")
    @Operation(summary = "Obtener imágenes del artículo", description = "Lista todas las imágenes de un artículo")
    public ResponseEntity<List<NewsImage>> getArticleImages(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer articleId) {
        List<NewsImage> images = newsImageService.getArticleImages(articleId);
        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Eliminar imagen", description = "Elimina una imagen específica")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "ID de la imagen") 
            @PathVariable Integer imageId) {
        try {
            newsImageService.deleteImage(imageId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{articleId}/images")
    @Operation(summary = "Eliminar todas las imágenes del artículo", description = "Elimina todas las imágenes de un artículo")
    public ResponseEntity<Void> deleteArticleImages(
            @Parameter(description = "ID del artículo") 
            @PathVariable Integer articleId) {
        try {
            newsImageService.deleteArticleImages(articleId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}