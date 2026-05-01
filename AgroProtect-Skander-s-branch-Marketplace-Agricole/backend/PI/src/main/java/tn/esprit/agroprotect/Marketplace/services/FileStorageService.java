package  tn.esprit.agroprotect.Marketplace.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir:./uploads}")  // Note the ":./uploads" default fallback
    private String uploadDir;

    private static final String[] ALLOWED_CONTENT_TYPES = {
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/gif",
            "image/webp",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            logger.info("Upload directory initialized at: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file, Long annonceId) {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(ALLOWED_CONTENT_TYPES).contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed. Allowed: PDF, JPEG, PNG, Word, Excel");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedFilename = annonceId + "_" + timestamp + "_" + originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        logger.debug("Storing file: original={}, sanitized={}", originalFilename, sanitizedFilename);

        Path announcePath;
        try {
            Path absoluteUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            announcePath = absoluteUploadDir.resolve(String.valueOf(annonceId));
            if (!Files.exists(announcePath)) {
                Files.createDirectories(announcePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create announce subdirectory", e);
        }

        Path targetPath = announcePath.resolve(sanitizedFilename).normalize();
        if (!targetPath.startsWith(announcePath)) {
            throw new IllegalArgumentException("Invalid file path: " + sanitizedFilename);
        }

        logger.debug("Target path: {}", targetPath);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }

        // Return relative path
        return annonceId + "/" + sanitizedFilename;
    }

    public void deleteFile(String filePath) {
        try {
            Path fullPath = Paths.get(uploadDir).resolve(filePath).normalize();
            boolean deleted = Files.deleteIfExists(fullPath);
            if (deleted) {
                logger.info("Deleted file: {}", fullPath);
            } else {
                logger.warn("File not found for deletion: {}", fullPath);
            }
        } catch (IOException e) {
            logger.error("Could not delete file: {}", filePath, e);
            throw new RuntimeException("Could not delete file", e);
        }
    }
}
