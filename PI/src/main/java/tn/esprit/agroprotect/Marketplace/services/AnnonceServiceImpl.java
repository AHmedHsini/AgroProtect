package com.example.agroprotect.services;

import com.example.agroprotect.entities.Annonce;
import com.example.agroprotect.entities.StatusAnnonce;
import com.example.agroprotect.entities.TypeAnnonce;
import com.example.agroprotect.repositories.AnnonceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnonceServiceImpl implements AnnonceService {

    @Autowired
    private AnnonceRepository annonceRepository;

    @Override
    public Annonce createAnnonce(Annonce annonce) {
        return annonceRepository.save(annonce);
    }

    @Override
    public Annonce updateAnnonce(Long id, Annonce annonce) {
        Annonce existingAnnonce = annonceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce not found"));

        existingAnnonce.setTitre(annonce.getTitre());
        existingAnnonce.setDescription(annonce.getDescription());
        existingAnnonce.setTypeAnnonce(annonce.getTypeAnnonce());
        existingAnnonce.setProjetId(annonce.getProjetId());
        existingAnnonce.setReferenceExterneId(annonce.getReferenceExterneId());

        return annonceRepository.save(existingAnnonce);
    }

    @Override
    public void deleteAnnonce(Long id) {
        annonceRepository.deleteById(id);
    }

    @Override
    public Annonce getAnnonceById(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annonce not found"));
    }

    @Override
    public List<Annonce> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    @Override
    public List<Annonce> getAnnoncesByCreateur(Long createurId) {
        return annonceRepository.findByCreateurId(createurId);
    }

    @Override
    public List<Annonce> getAnnoncesByStatus(StatusAnnonce status) {
        return annonceRepository.findByStatus(status);
    }

    @Override
    public List<Annonce> getAnnoncesByType(TypeAnnonce typeAnnonce) {
        return annonceRepository.findByTypeAnnonce(typeAnnonce);
    }

    @Override
    public Annonce updateStatus(Long id, StatusAnnonce status) {
        Annonce annonce = getAnnonceById(id);
        annonce.setStatus(status);
        return annonceRepository.save(annonce);
    }
}