package tn.esprit.agroprotect.Marketplace.services;

import tn.esprit.agroprotect.Marketplace.dtos.AnnonceSearchResponse;
import tn.esprit.agroprotect.Marketplace.dtos.FundingProgressDTO;
import tn.esprit.agroprotect.Marketplace.dtos.SearchAnnonceRequest;
import tn.esprit.agroprotect.Marketplace.entities.Annonce;
import tn.esprit.agroprotect.Marketplace.entities.StatusAnnonce;
import tn.esprit.agroprotect.Marketplace.entities.TypeAnnonce;

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

    FundingProgressDTO getFundingProgress(Long annonceId);

    void checkAndCloseAnnouncement(Long annonceId, Long investisseurId);


    void autoCloseIfFullyFunded(Long annonceId);


    AnnonceSearchResponse searchAnnonces(SearchAnnonceRequest request);
}