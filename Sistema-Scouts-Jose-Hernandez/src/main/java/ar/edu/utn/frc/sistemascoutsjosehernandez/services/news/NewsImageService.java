package ar.edu.utn.frc.sistemascoutsjosehernandez.services.news;

import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsArticle;
import ar.edu.utn.frc.sistemascoutsjosehernandez.entities.news.NewsImage;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsArticleRepository;
import ar.edu.utn.frc.sistemascoutsjosehernandez.repositories.news.NewsImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsImageService {

    private final NewsImageRepository newsImageRepository;
    private final NewsArticleRepository newsArticleRepository;

    @Value("${app.news.images.upload-dir:uploads/news}")
    private String uploadDir;

    @Value("${app.news.images.max-size:5242880}")
    private long maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public NewsImage uploadImage(Integer articleId, MultipartFile file, String altText, String caption) {
        NewsArticle article = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename());
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            NewsImage newsImage = NewsImage.builder()
                    .article(article)
                    .imageUrl("/uploads/news/" + fileName)
                    .altText(altText)
                    .caption(caption)
                    .build();

            return newsImageRepository.save(newsImage);

        } catch (IOException e) {
            throw new RuntimeException("Error subiendo imagen: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<NewsImage> getArticleImages(Integer articleId) {
        return newsImageRepository.findByArticleIdOrderByCreatedAtAsc(articleId);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public void deleteImage(Integer imageId) {
        NewsImage image = newsImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(extractFileName(image.getImageUrl()));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error eliminando archivo físico: " + e.getMessage());
        }

        newsImageRepository.delete(image);
    }

    @PreAuthorize("hasRole('NEWS_MANAGER') or hasRole('ADMIN')")
    public void deleteArticleImages(Integer articleId) {
        List<NewsImage> images = newsImageRepository.findByArticleIdOrderByCreatedAtAsc(articleId);
        
        for (NewsImage image : images) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(extractFileName(image.getImageUrl()));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Error eliminando archivo físico: " + e.getMessage());
            }
        }

        newsImageRepository.deleteByArticleId(articleId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("El archivo no puede estar vacío");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new RuntimeException("Nombre de archivo inválido");
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new RuntimeException("Tipo de archivo no permitido. Permitidos: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String generateFileName(String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + fileExtension;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    private String extractFileName(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }
}