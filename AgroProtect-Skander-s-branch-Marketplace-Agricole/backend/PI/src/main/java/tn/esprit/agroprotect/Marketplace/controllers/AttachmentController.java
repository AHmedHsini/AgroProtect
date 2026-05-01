package  tn.esprit.agroprotect.Marketplace.controllers;

import  tn.esprit.agroprotect.Marketplace.entities.Attachment;
import  tn.esprit.agroprotect.Marketplace.entities.AttachmentStatus;
import  tn.esprit.agroprotect.Marketplace.services.AttachmentService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.FileNotFoundException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping({"/Annonce/attachments"})
public class AttachmentController {

    AttachmentService attachmentService;

    @PostMapping("/upload/{annonceId}")
    public ResponseEntity<Attachment> upload(
            @PathVariable Long annonceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "OTHER") String category
    ) {
        Attachment attachment = attachmentService.uploadFile(annonceId, file, userId, category);
        return ResponseEntity.ok(attachment);
    }


    @GetMapping("/{annonceId}")
    public List<Attachment> getByAnnonce(@PathVariable Long annonceId) {
        return attachmentService.getAttachmentsByAnnonce(annonceId);
    }


    @GetMapping("/download/{attachmentId}")
    public ResponseEntity<Resource> download(@PathVariable Long attachmentId, HttpServletRequest request)
            throws FileNotFoundException {
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);

        Resource resource = attachmentService.downloadFile(attachment.getFilePath());

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (Exception e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }


    @PutMapping("/approve/{attachmentId}")
    public Attachment approve(@PathVariable Long attachmentId, @RequestParam Long adminId) {
        return attachmentService.approveAttachment(attachmentId, adminId);
    }


    @PutMapping("/reject/{attachmentId}")
    public Attachment reject(@PathVariable Long attachmentId, @RequestParam Long adminId,
            @RequestParam(required = false) String reason) {
        return attachmentService.rejectAttachment(attachmentId, adminId, reason);
    }


    @DeleteMapping("/delete/{attachmentId}")
    public ResponseEntity<Void> delete(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
