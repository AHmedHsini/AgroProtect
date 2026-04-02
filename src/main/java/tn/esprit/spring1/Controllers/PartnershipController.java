package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.IPartnershipService;
import tn.esprit.spring1.entities.Partnership;

import java.util.List;

@RestController
@RequestMapping("/Partnership")
@AllArgsConstructor
public class PartnershipController {

    IPartnershipService chService;

    @PostMapping("/ajouterPartnership")
    public Partnership ajouterPartnership(@RequestBody Partnership Partnership){
        return chService.addPartnership(Partnership);
    }

    @PutMapping("/updatePartnership")
    public Partnership updatePartnership(@RequestBody Partnership Partnership){
        return chService.updatePartnership(Partnership);
    }

    @GetMapping("/getPartnership/{id}")
    public Partnership getPartnershipById(@PathVariable Long id){
        return chService.getPartnershipById(id);
    }

    @GetMapping("/getAllPartnership")
    public List<Partnership> getAllPartnerships(){
        return chService.getAllPartnership();
    }


    @DeleteMapping("/supprimerPartnership/{id}")
    public void deletePartnership(@PathVariable Long id){
        chService.deletePartnership(id);
    }

    @GetMapping("/active")
    public List<Partnership> getActive() {
        return chService.getActivePartnerships();
    }

    @GetMapping("/expired")
    public List<Partnership> getExpired() {
        return chService.getExpiredPartnerships();
    }

    @GetMapping("/averageDuration")
    public Double getAverageDuration() {
        return chService.getAveragePartnershipDuration();
    }

    @GetMapping("/countByPartner/{id}")
    public Long countByPartner(@PathVariable Long id) {
        return chService.countPartnershipsByPartner(id);
    }


}
