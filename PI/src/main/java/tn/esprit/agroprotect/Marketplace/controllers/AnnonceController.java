package com.example.agroprotect.controllers;

import com.example.agroprotect.entities.Annonce;
import com.example.agroprotect.entities.StatusAnnonce;
import com.example.agroprotect.entities.TypeAnnonce;
import com.example.agroprotect.services.AnnonceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping({"/Annonce"})
public class AnnonceController {

    AnnonceService annonceService;

    @PostMapping("/addAnnonce")
    public Annonce addAnnonce(@RequestBody Annonce annonce) {
        return annonceService.createAnnonce(annonce);
    }

    @GetMapping("/getAll")
    public List<Annonce> getAllAnnonce() {
        return annonceService.getAllAnnonces();
    }

    @GetMapping("/getById/{id}")
    public Annonce getById(@PathVariable Long id) {
        return annonceService.getAnnonceById(id);
    }

    @PutMapping("/updateAnnonce/{id}")
    public Annonce updateAnnonce(@PathVariable Long id, @RequestBody Annonce annonce) {
        return annonceService.updateAnnonce(id, annonce);
    }

    @DeleteMapping("/deleteAnnonce/{id}")
    public void deleteAnnonce(@PathVariable Long id) {
        annonceService.deleteAnnonce(id);
    }

    @GetMapping("/getByCreateur/{createurId}")
    public List<Annonce> getByCreateur(@PathVariable Long createurId) {
        return annonceService.getAnnoncesByCreateur(createurId);
    }

    @GetMapping("/getByStatus/{status}")
    public List<Annonce> getByStatus(@PathVariable StatusAnnonce status) {
        return annonceService.getAnnoncesByStatus(status);
    }

    @GetMapping("/getByType/{type}")
    public List<Annonce> getByType(@PathVariable TypeAnnonce type) {
        return annonceService.getAnnoncesByType(type);
    }

    @PutMapping("/updateStatus/{id}/{status}")
    public Annonce updateStatus(@PathVariable Long id, @PathVariable StatusAnnonce status) {
        return annonceService.updateStatus(id, status);
    }
}