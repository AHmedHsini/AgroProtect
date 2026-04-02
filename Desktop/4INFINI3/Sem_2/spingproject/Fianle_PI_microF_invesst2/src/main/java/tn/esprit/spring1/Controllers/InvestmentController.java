package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.IInvestmentService;
import tn.esprit.spring1.entities.Investment;
import tn.esprit.spring1.entities.InvestmentStatus;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/Investment")
@AllArgsConstructor
public class InvestmentController {

    IInvestmentService chService;

    @PostMapping("/ajouterInvestment")
    public Investment ajouterInvestment(@RequestBody Investment Investment){
        return chService.addInvestment(Investment);
    }

    @PutMapping("/updateInvestment")
    public Investment updateInvestment(@RequestBody Investment Investment){
        return chService.updateInvestment(Investment);
    }

    @GetMapping("/getInvestment/{id}")
    public Investment getInvestmentById(@PathVariable Long id){
        return chService.getInvestmentById(id);
    }

    @GetMapping("/getAllInvestment")
    public List<Investment> getAllInvestments(){
        return chService.getAllInvestment();
    }


    @DeleteMapping("/supprimerInvestment/{id}")
    public void deleteInvestment(@PathVariable Long id){
        chService.deleteInvestment(id);
    }

    @GetMapping("/byStatus/{status}")
    public List<Investment> getByStatus(@PathVariable InvestmentStatus status) {
        return chService.getInvestmentsByStatus(status);
    }

    @GetMapping("/greaterThan/{amount}")
    public List<Investment> getGreaterThan(@PathVariable double amount) {
        return chService.getInvestmentsGreaterThan(amount);
    }

    @GetMapping("/between")
    public List<Investment> getBetweenDates(@RequestParam Date start,
                                            @RequestParam Date end) {
        return chService.getInvestmentsBetweenDates(start, end);
    }

    @GetMapping("/byInvestor/{idInvestor}")
    public List<Investment> getByInvestor(@PathVariable Long idInvestor) {
        return chService.getInvestmentsByInvestor(idInvestor);
    }

    @GetMapping("/total/{idInvestor}")
    public Double getTotal(@PathVariable Long idInvestor) {
        return chService.getTotalInvestedByInvestor(idInvestor);
    }

    // Moyenne des equity
    @GetMapping("/averageEquity")
    public Double getAverageEquity() {
        return chService.getAverageEquity();
    }

    // Vérifier si un investisseur peut investir un montant donné
    @GetMapping("/canInvest")
    public boolean canInvest(@RequestParam Long idInvestor,
                             @RequestParam double amount) {
        return chService.canInvestorInvest(idInvestor, amount);
    }

    @GetMapping("/caisse")
    public Double getCaisse() {
        return chService.getTotalInvested();
    }

}
