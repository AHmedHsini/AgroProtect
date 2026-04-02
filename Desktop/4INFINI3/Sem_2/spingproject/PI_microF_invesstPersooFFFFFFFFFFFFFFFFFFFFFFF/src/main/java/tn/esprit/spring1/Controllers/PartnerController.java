package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.IPartnerService;
import tn.esprit.spring1.entities.Partner;
import tn.esprit.spring1.entities.PartnerType;

import java.util.List;

@RestController
@RequestMapping("/Partner")
@AllArgsConstructor
public class PartnerController {

    IPartnerService chService;

    @PostMapping("/ajouterPartner")
    public Partner ajouterPartner(@RequestBody Partner Partner){
        return chService.addPartner(Partner);
    }

    @PutMapping("/updatePartner")
    public Partner updatePartner(@RequestBody Partner Partner){
        return chService.updatePartner(Partner);
    }

    @GetMapping("/getPartner/{id}")
    public Partner getPartnerById(@PathVariable Long id){
        return chService.getPartnerById(id);
    }

    @GetMapping("/getAllPartner")
    public List<Partner> getAllPartners(){
        return chService.getAllPartner();
    }


    @DeleteMapping("/supprimerPartner/{id}")
    public void deletePartner(@PathVariable Long id){
        chService.deletePartner(id);
    }

    @GetMapping("/byRegion/{region}")
    public List<Partner> getByRegion(@PathVariable String region) {
        return chService.getPartnersByRegion(region);
    }

    @GetMapping("/byType/{type}")
    public List<Partner> getByType(@PathVariable PartnerType type) {
        return chService.getPartnersByType(type);
    }

    @GetMapping("/mostInvolved")
    public List<Partner> getMostInvolved() {
        return chService.getMostInvolvedPartners();
    }

    @GetMapping("/activePartnerships/{id}")
    public Long getActivePartnerships(@PathVariable Long id) {
        return chService.getActivePartnershipsCount(id);
    }

}
