package tn.esprit.scoringaideservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.scoringaideservice.entity.DonneeClimatique;
import java.util.List;   // ✅ AJOUTER CET IMPORT

public interface DonneeClimatiqueRepository
        extends JpaRepository<DonneeClimatique, Long> {

    List<DonneeClimatique>
    findByTerrainAgricoleIdOrderByAnneeDesc(Long terrainId);

}
