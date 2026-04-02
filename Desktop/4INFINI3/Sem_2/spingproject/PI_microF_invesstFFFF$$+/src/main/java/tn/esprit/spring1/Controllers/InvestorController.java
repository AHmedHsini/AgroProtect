package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.IInvestorService;
import tn.esprit.spring1.entities.Investor;
import java.util.List;

@RestController
@RequestMapping("/Investor")
@AllArgsConstructor
public class InvestorController {

    IInvestorService chService;

    @PostMapping("/ajouterInvestor")
    public Investor ajouterInvestor(@RequestBody Investor Investor){
        return chService.addInvestor(Investor);
    }

    @PutMapping("/updateInvestor")
    public Investor updateInvestor(@RequestBody Investor Investor){
        return chService.updateInvestor(Investor);
    }

    @GetMapping("/getInvestor/{id}")
    public Investor getInvestorById(@PathVariable Long id){
        return chService.getInvestorById(id);
    }

    @GetMapping("/getAllInvestor")
    public List<Investor> getAllInvestors(){
        return chService.getAllInvestor();
    }


    @DeleteMapping("/supprimerInvestor/{id}")
    public void deleteInvestor(@PathVariable Long id){
        chService.deleteInvestor(id);
    }

    @GetMapping("/search/{keyword}")
    public List<Investor> searchInvestor(@PathVariable String keyword) {
        return chService.searchByName(keyword);
    }

    @GetMapping("/sortedByCapital")
    public List<Investor> sortedInvestors() {
        return chService.getAllInvestorsSortedByCapital();
    }


}
