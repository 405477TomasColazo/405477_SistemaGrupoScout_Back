package ar.edu.utn.frc.sistemascoutsjosehernandez.services.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.CreateNewsCategoryDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.dtos.news.NewsCategoryDto;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsCategory;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsCategoryService {

    private final NewsCategoryRepository newsCategoryRepository;

    @Transactional(readOnly = true)
    public List<NewsCategoryDto> getAllCategories() {
        return newsCategoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<NewsCategoryDto> getCategoryById(Integer id) {
        return newsCategoryRepository.findById(id).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Optional<NewsCategoryDto> getCategoryBySlug(String slug) {
        return newsCategoryRepository.findBySlug(slug).map(this::convertToDto);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsCategoryDto createCategory(CreateNewsCategoryDto createDto) {
        if (newsCategoryRepository.existsByName(createDto.getName())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        String slug = generateUniqueSlug(createDto.getName());

        NewsCategory category = NewsCategory.builder()
                .name(createDto.getName())
                .slug(slug)
                .description(createDto.getDescription())
                .color(createDto.getColor())
                .build();

        NewsCategory savedCategory = newsCategoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsCategoryDto updateCategory(Integer id, CreateNewsCategoryDto updateDto) {
        NewsCategory category = newsCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (updateDto.getName() != null && !updateDto.getName().equals(category.getName())) {
            if (newsCategoryRepository.existsByName(updateDto.getName())) {
                throw new RuntimeException("Ya existe una categoría con ese nombre");
            }
            category.setName(updateDto.getName());
            category.setSlug(generateUniqueSlug(updateDto.getName()));
        }

        if (updateDto.getDescription() != null) {
            category.setDescription(updateDto.getDescription());
        }

        if (updateDto.getColor() != null) {
            category.setColor(updateDto.getColor());
        }

        NewsCategory savedCategory = newsCategoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public void deleteCategory(Integer id) {
        NewsCategory category = newsCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (!category.getArticles().isEmpty()) {
            throw new RuntimeException("No se puede eliminar una categoría que tiene artículos asociados");
        }

        newsCategoryRepository.delete(category);
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String slug = baseSlug;
        int counter = 1;

        while (newsCategoryRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private NewsCategoryDto convertToDto(NewsCategory category) {
        return NewsCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .color(category.getColor())
                .build();
    }
}