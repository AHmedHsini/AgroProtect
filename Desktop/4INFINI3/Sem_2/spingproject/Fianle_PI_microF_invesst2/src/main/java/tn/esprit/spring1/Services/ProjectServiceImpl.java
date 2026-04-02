package tn.esprit.spring1.Services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.Project;
import tn.esprit.spring1.repositories.ProjectRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    ProjectRepository chrep;

    @Override
    public Project addProject(Project project) {
        return chrep.save(project);
    }

    @Override
    public Project updateProject(Project project) {
        return chrep.save(project);
    }

    @Override
    public void deleteProject(Long id) {
        chrep.deleteById(id);
    }

    @Override
    public Project getProjectById(Long id) {
        return chrep.findById(id).orElse(null);
    }

    @Override
    public List<Project> getAllProjects() {
        return chrep.findAll();
    }

    //RECHERCHE

    @Override
    public List<Project> searchByName(String keyword) {
        return chrep.findByNameContaining(keyword);
    }

    @Override
    public List<Project> getBySector(String sector) {
        return chrep.findBySector(sector);
    }

    //METIERS AVANCES

    @Override
    public Double getFundingProgress(Long idProject) {
        Project p = getProjectById(idProject);
        if (p == null || p.getFundingGoal() == 0) return 0.0;

        return (p.getCollectedAmount() / p.getFundingGoal()) * 100;
    }

    @Override
    public boolean isFullyFunded(Long idProject) {
        Project p = getProjectById(idProject);
        return p != null && p.getCollectedAmount() >= p.getFundingGoal();
    }

    @Override
    public List<Project> getProjectsSortedByCollectedAmount() {
        return chrep.findAll(Sort.by(Sort.Direction.DESC, "collectedAmount"));
    }

    @Override
    public Double getFundingPercentage(Long idProject) {

        Project project = chrep.findById(idProject).orElse(null);

        if (project == null || project.getFundingGoal() == 0) {
            return 0.0;
        }

        return (project.getCollectedAmount() / project.getFundingGoal()) * 100;
    }

    @Override
    public List<Project> getFullyFundedProjects() {
        return chrep.getFullyFundedProjects();
    }

    @Override
    public List<Project> getProjectsAbove80Percent() {
        return chrep.getProjectsAbove80Percent();
    }

}
