package  tn.esprit.agroprotect.Marketplace.controllers;

import  tn.esprit.agroprotect.Marketplace.dtos.AnnonceSearchResponse;
import  tn.esprit.agroprotect.Marketplace.dtos.FundingProgressDTO;
import  tn.esprit.agroprotect.Marketplace.dtos.SearchAnnonceRequest;
import  tn.esprit.agroprotect.Marketplace.entities.Annonce;
import  tn.esprit.agroprotect.Marketplace.entities.StatusAnnonce;
import  tn.esprit.agroprotect.Marketplace.entities.TypeAnnonce;
import  tn.esprit.agroprotect.Marketplace.services.AnnonceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
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
    public Annonce getById(@PathVariable("id") Long id) {
        return annonceService.getAnnonceById(id);
    }

    @PutMapping("/updateAnnonce/{id}")
    public Annonce updateAnnonce(@PathVariable("id") Long id, @RequestBody Annonce annonce) {
        return annonceService.updateAnnonce(id, annonce);
    }

    @DeleteMapping("/deleteAnnonce/{id}")
    public void deleteAnnonce(@PathVariable("id") Long id) {
        annonceService.deleteAnnonce(id);
    }

    @GetMapping("/getByCreateur/{createurId}")
    public List<Annonce> getByCreateur(@PathVariable("createurId") Long createurId) {
        return annonceService.getAnnoncesByCreateur(createurId);
    }

    @GetMapping("/getByStatus/{status}")
    public List<Annonce> getByStatus(@PathVariable("status") StatusAnnonce status) {
        return annonceService.getAnnoncesByStatus(status);
    }

    @GetMapping("/getByType/{type}")
    public List<Annonce> getByType(@PathVariable("type") TypeAnnonce type) {
        return annonceService.getAnnoncesByType(type);
    }

    @PutMapping("/updateStatus/{id}/{status}")
    public Annonce updateStatus(@PathVariable("id") Long id, @PathVariable("status") StatusAnnonce status) {
        return annonceService.updateStatus(id, status);
    }

    @GetMapping("/fundingProgress/{annonceId}")
    public FundingProgressDTO getFundingProgress(@PathVariable("annonceId") Long annonceId) {
        return annonceService.getFundingProgress(annonceId);
    }

    @PostMapping("/search")
    public AnnonceSearchResponse searchAnnonces(@Valid @RequestBody SearchAnnonceRequest request) {
        return annonceService.searchAnnonces(request);
    }
}