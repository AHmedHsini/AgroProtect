package  tn.esprit.agroprotect.Marketplace.repositories;

import  tn.esprit.agroprotect.Marketplace.entities.Annonce;
import  tn.esprit.agroprotect.Marketplace.entities.StatusAnnonce;
import  tn.esprit.agroprotect.Marketplace.entities.TypeAnnonce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    List<Annonce> findByCreateurId(Long createurId);

    List<Annonce> findByStatus(StatusAnnonce status);

    List<Annonce> findByTypeAnnonce(TypeAnnonce typeAnnonce);


    List<Annonce> findByStatusAndTypeAnnonce(StatusAnnonce status, TypeAnnonce typeAnnonce);

    Page<Annonce> findByStatus(StatusAnnonce status, Pageable pageable);

    Page<Annonce> findByTypeAnnonce(TypeAnnonce typeAnnonce, Pageable pageable);

    Page<Annonce> findByStatusAndTypeAnnonce(StatusAnnonce status, TypeAnnonce typeAnnonce, Pageable pageable);

    Page<Annonce> findByLocationContainingIgnoreCase(String location, Pageable pageable);


    @Query("SELECT a FROM Annonce a WHERE " +
            "(:search IS NULL OR LOWER(a.titre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:type IS NULL OR a.typeAnnonce = :type) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:location IS NULL OR LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:minAmount IS NULL OR a.targetAmount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR a.targetAmount <= :maxAmount)")
    Page<Annonce> searchAnnonces(
            @Param("search") String search,
            @Param("type") TypeAnnonce type,
            @Param("status") StatusAnnonce status,
            @Param("location") String location,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount,
            Pageable pageable
    );
}