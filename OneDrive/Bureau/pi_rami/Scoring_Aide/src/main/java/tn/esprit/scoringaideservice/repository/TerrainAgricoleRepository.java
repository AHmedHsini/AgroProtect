package tn.esprit.scoringaideservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.scoringaideservice.entity.TerrainAgricole;

import java.util.List;


public interface TerrainAgricoleRepository
        extends JpaRepository<TerrainAgricole, Long> {

    List<TerrainAgricole> findByRegion(String region);

}
