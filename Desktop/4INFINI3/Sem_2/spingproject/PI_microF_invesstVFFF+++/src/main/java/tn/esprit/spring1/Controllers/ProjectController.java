package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.IProjectService;
import tn.esprit.spring1.entities.Project;

import java.util.List;

@RestController
@RequestMapping("/Project")
@AllArgsConstructor
public class ProjectController {

    IProjectService chService;

    @PostMapping("/add")
    public Project add(@RequestBody Project p){
        return chService.addProject(p);
    }

    @GetMapping("/all")
    public List<Project> getAll(){
        return chService.getAllProjects();
    }

    @GetMapping("/search/{keyword}")
    public List<Project> search(@PathVariable String keyword){
        return chService.searchByName(keyword);
    }

    @GetMapping("/sector/{sector}")
    public List<Project> bySector(@PathVariable String sector){
        return chService.getBySector(sector);
    }

    @GetMapping("/progress/{id}")
    public Double progress(@PathVariable Long id){
        return chService.getFundingProgress(id);
    }

    @GetMapping("/fullyFunded/{id}")
    public boolean fullyFunded(@PathVariable Long id){
        return chService.isFullyFunded(id);
    }

    @GetMapping("/sorted")
    public List<Project> sorted(){
        return chService.getProjectsSortedByCollectedAmount();
    }

    @GetMapping("/fundingPercentage/{idProject}")
    public Double getFundingPercentage(@PathVariable Long idProject) {
        return chService.getFundingPercentage(idProject);
    }

    @GetMapping("/fullyFunded")
    public List<Project> getFullyFundedProjects() {
        return chService.getFullyFundedProjects();
    }

    @GetMapping("/above80")
    public List<Project> getProjectsAbove80() {
        return chService.getProjectsAbove80Percent();
    }
}
