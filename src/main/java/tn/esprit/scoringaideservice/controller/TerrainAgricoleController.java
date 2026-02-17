package tn.esprit.scoringaideservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.scoringaideservice.entity.TerrainAgricole;
import tn.esprit.scoringaideservice.repository.TerrainAgricoleRepository;

import java.util.List;

@RestController
@RequestMapping("/api/terrain")
@RequiredArgsConstructor
public class TerrainAgricoleController {

    private final TerrainAgricoleRepository terrainRepo;

    @GetMapping
    public List<TerrainAgricole> getAll() {
        return terrainRepo.findAll();
    }

    @GetMapping("/{id}")
    public TerrainAgricole getById(@PathVariable Long id) {
        return terrainRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Terrain introuvable"));
    }

    @PostMapping
    public TerrainAgricole create(@RequestBody TerrainAgricole terrain) {
        return terrainRepo.save(terrain);
    }

    @PutMapping("/{id}")
    public TerrainAgricole update(@PathVariable Long id,
                                  @RequestBody TerrainAgricole terrain) {

        TerrainAgricole t = terrainRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Terrain introuvable"));

        t.setSurface(terrain.getSurface());
        t.setTypeSol(terrain.getTypeSol());
        t.setRegion(terrain.getRegion());

        return terrainRepo.save(t);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        terrainRepo.deleteById(id);
    }
}
