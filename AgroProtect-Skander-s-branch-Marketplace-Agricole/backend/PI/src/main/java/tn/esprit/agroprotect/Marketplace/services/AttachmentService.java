package  tn.esprit.agroprotect.Marketplace.services;

import  tn.esprit.agroprotect.Marketplace.entities.Attachment;
import  tn.esprit.agroprotect.Marketplace.entities.AttachmentStatus;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

public interface AttachmentService {


    Attachment uploadFile(Long annonceId, MultipartFile file, Long userId, String category);

    List<Attachment> getAttachmentsByAnnonce(Long annonceId);

    Attachment getAttachmentById(Long attachmentId);

    Page<Attachment> getAttachmentsByStatus(AttachmentStatus status, Pageable pageable);

    Attachment approveAttachment(Long attachmentId, Long adminId);

    Attachment rejectAttachment(Long attachmentId, Long adminId, String reason);

    void deleteAttachment(Long attachmentId);

    Resource downloadFile(String filePath) throws FileNotFoundException;
}
