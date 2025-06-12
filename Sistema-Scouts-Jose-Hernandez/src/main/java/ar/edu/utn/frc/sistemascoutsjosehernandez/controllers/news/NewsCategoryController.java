package ar.edu.utn.frc.sistemascoutsjosehernandez.controllers.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.CreateNewsCategoryDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.NewsCategoryDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.services.news.NewsCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news/categories")
@RequiredArgsConstructor
@Tag(name = "News Categories", description = "API para gestión de categorías de noticias")
@CrossOrigin(origins = "http://localhost:4200")
public class NewsCategoryController {

    private final NewsCategoryService newsCategoryService;

    @GetMapping
    @Operation(summary = "Obtener todas las categorías", description = "Lista todas las categorías disponibles")
    public ResponseEntity<List<NewsCategoryDto>> getAllCategories() {
        List<NewsCategoryDto> categories = newsCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría específica por su ID")
    public ResponseEntity<NewsCategoryDto> getCategoryById(
            @Parameter(description = "ID de la categoría") 
            @PathVariable Integer id) {
        return newsCategoryService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Obtener categoría por slug", description = "Obtiene una categoría específica por su slug")
    public ResponseEntity<NewsCategoryDto> getCategoryBySlug(
            @Parameter(description = "Slug de la categoría") 
            @PathVariable String slug) {
        return newsCategoryService.getCategoryBySlug(slug)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría de noticias")
    public ResponseEntity<NewsCategoryDto> createCategory(
            @Valid @RequestBody CreateNewsCategoryDto createDto) {
        try {
            NewsCategoryDto createdCategory = newsCategoryService.createCategory(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente")
    public ResponseEntity<NewsCategoryDto> updateCategory(
            @Parameter(description = "ID de la categoría") 
            @PathVariable Integer id,
            @Valid @RequestBody CreateNewsCategoryDto updateDto) {
        try {
            NewsCategoryDto updatedCategory = newsCategoryService.updateCategory(id, updateDto);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría permanentemente")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la categoría") 
            @PathVariable Integer id) {
        try {
            newsCategoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}