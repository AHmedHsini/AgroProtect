package  tn.esprit.agroprotect.Marketplace.services;

import  tn.esprit.agroprotect.Marketplace.entities.Annonce;
import  tn.esprit.agroprotect.Marketplace.entities.Attachment;
import  tn.esprit.agroprotect.Marketplace.entities.AttachmentStatus;
import  tn.esprit.agroprotect.Marketplace.exceptions.ResourceNotFoundException;
import  tn.esprit.agroprotect.Marketplace.repositories.AnnonceRepository;
import  tn.esprit.agroprotect.Marketplace.repositories.AttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional
    public Attachment uploadFile(Long annonceId, MultipartFile file, Long userId, String category) {
        try {
            logger.info("[BEGIN] Upload attachment: annonceId={}, userId={}, fileName={}, fileSize={}, category={}",
                    annonceId, userId, file.getOriginalFilename(), file.getSize(), category);

            Annonce annonce = annonceRepository.findById(annonceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Annonce not found: " + annonceId));
            logger.debug("Found annonce: titre={}, createurId={}", annonce.getTitre(), annonce.getCreateurId());

            logger.debug("Calling fileStorageService.storeFile()");
            String filePath = fileStorageService.storeFile(file, annonceId);
            logger.debug("File stored at path: {}", filePath);

            Attachment attachment = new Attachment();
            attachment.setAnnonce(annonce);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(filePath);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setCategory(category != null ? category : "OTHER");
            attachment.setStatus(AttachmentStatus.PENDING);
            attachment.setUploadedBy(userId);

            Attachment saved = attachmentRepository.save(attachment);
            logger.info("[SUCCESS] Attachment uploaded: attachmentId={}, annonceId={}, category={}, filePath={}",
                    saved.getId(), annonceId, category, filePath);
            return saved;
        } catch (Exception e) {
            logger.error("[ERROR] Upload failed for annonceId={}, userId={}, fileName={}",
                    annonceId, userId, file.getOriginalFilename(), e);
            throw e;
        }
    }


    @Override
    public List<Attachment> getAttachmentsByAnnonce(Long annonceId) {
        return attachmentRepository.findByAnnonceId(annonceId);
    }

    @Override
    public Attachment getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));
    }

    @Override
    public Page<Attachment> getAttachmentsByStatus(AttachmentStatus status, Pageable pageable) {
        return attachmentRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public Attachment approveAttachment(Long attachmentId, Long adminId) {
        Attachment attachment = getAttachmentById(attachmentId);

        if (attachment.getStatus() != AttachmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING attachments can be approved. Current status: " + attachment.getStatus());
        }

        attachment.setStatus(AttachmentStatus.APPROVED);
        attachment.setReviewedBy(adminId);
        attachment.setReviewedAt(LocalDateTime.now());
        return attachmentRepository.save(attachment);
    }

    @Override
    @Transactional
    public Attachment rejectAttachment(Long attachmentId, Long adminId, String reason) {
        Attachment attachment = getAttachmentById(attachmentId);

        if (attachment.getStatus() != AttachmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING attachments can be rejected. Current status: " + attachment.getStatus());
        }

        attachment.setStatus(AttachmentStatus.REJECTED);
        attachment.setReviewedBy(adminId);
        attachment.setReviewedAt(LocalDateTime.now());
        attachment.setRejectionReason(reason);
        return attachmentRepository.save(attachment);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = getAttachmentById(attachmentId);
        // Delete physical file
        fileStorageService.deleteFile(attachment.getFilePath());
        // Delete record
        attachmentRepository.delete(attachment);
    }

    @Override
    public Resource downloadFile(String filePath) throws FileNotFoundException {
        try {
            Path path = Paths.get(System.getProperty("user.dir")).resolve("uploads").resolve(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
    }
}
