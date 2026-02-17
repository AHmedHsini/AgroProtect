package com.example.agroprotect.repositories;

import com.example.agroprotect.entities.Annonce;
import com.example.agroprotect.entities.StatusAnnonce;
import com.example.agroprotect.entities.TypeAnnonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    List<Annonce> findByCreateurId(Long createurId);

    List<Annonce> findByStatus(StatusAnnonce status);

    List<Annonce> findByTypeAnnonce(TypeAnnonce typeAnnonce);

    List<Annonce> findByProjetId(Long projetId);

    List<Annonce> findByStatusAndTypeAnnonce(StatusAnnonce status, TypeAnnonce typeAnnonce);
}