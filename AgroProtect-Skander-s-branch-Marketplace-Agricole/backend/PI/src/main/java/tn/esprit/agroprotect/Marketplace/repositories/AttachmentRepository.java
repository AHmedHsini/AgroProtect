package  tn.esprit.agroprotect.Marketplace.repositories;

import  tn.esprit.agroprotect.Marketplace.entities.Attachment;
import  tn.esprit.agroprotect.Marketplace.entities.AttachmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByAnnonceId(Long annonceId);

    List<Attachment> findByAnnonceIdAndStatus(Long annonceId, AttachmentStatus status);

    Page<Attachment> findByStatus(AttachmentStatus status, Pageable pageable);



    long countByAnnonceIdAndStatus(Long annonceId, AttachmentStatus status);
}
