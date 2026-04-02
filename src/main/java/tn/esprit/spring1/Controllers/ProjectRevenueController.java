package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.IProjectRevenueService;
import tn.esprit.spring1.entities.ProjectRevenue;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/ProjectRevenue")
@AllArgsConstructor
public class ProjectRevenueController {

    IProjectRevenueService chService;

    @PostMapping("/add")
    public ProjectRevenue add(@RequestBody ProjectRevenue revenue){
        return chService.addRevenue(revenue);
    }

    @GetMapping("/byProject/{id}")
    public List<ProjectRevenue> getByProject(@PathVariable Long id){
        return chService.getByProject(id);
    }

    @GetMapping("/total/{id}")
    public Double total(@PathVariable Long id){
        return chService.getTotalRevenue(id);
    }

    @GetMapping("/expenses/{id}")
    public Double expenses(@PathVariable Long id){
        return chService.getTotalExpenses(id);
    }

    @GetMapping("/profit/{id}")
    public Double profit(@PathVariable Long id){
        return chService.getProfit(id);
    }

    @GetMapping("/roi/{id}")
    public Double roi(@PathVariable Long id){
        return chService.getROI(id);
    }

    @GetMapping("/between")
    public List<ProjectRevenue> between(@RequestParam Date start,
                                        @RequestParam Date end){
        return chService.getBetweenDates(start,end);
    }

    @GetMapping("/greaterThan/{amount}")
    public List<ProjectRevenue> greaterThan(@PathVariable Double amount){
        return chService.getRevenueGreaterThan(amount);
    }
}
