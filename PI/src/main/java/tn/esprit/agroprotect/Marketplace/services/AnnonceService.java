package com.example.agroprotect.services;

import com.example.agroprotect.entities.Annonce;
import com.example.agroprotect.entities.StatusAnnonce;
import com.example.agroprotect.entities.TypeAnnonce;

import java.util.List;

public interface AnnonceService {

    Annonce createAnnonce(Annonce annonce);

    Annonce updateAnnonce(Long id, Annonce annonce);

    void deleteAnnonce(Long id);

    Annonce getAnnonceById(Long id);

    List<Annonce> getAllAnnonces();

    List<Annonce> getAnnoncesByCreateur(Long createurId);

    List<Annonce> getAnnoncesByStatus(StatusAnnonce status);

    List<Annonce> getAnnoncesByType(TypeAnnonce typeAnnonce);

    Annonce updateStatus(Long id, StatusAnnonce status);
}