package tn.esprit.spring1.Services;

import tn.esprit.spring1.entities.Project;

import java.util.List;

public interface IProjectService {

    Project addProject(Project project);

    Project updateProject(Project project);

    void deleteProject(Long id);

    Project getProjectById(Long id);

    List<Project> getAllProjects();

    //FILTRES
    List<Project> searchByName(String keyword);

    List<Project> getBySector(String sector);

    //METIERS AVANCES
    Double getFundingProgress(Long idProject);

    boolean isFullyFunded(Long idProject);

    List<Project> getProjectsSortedByCollectedAmount();

    Double getFundingPercentage(Long idProject);

    List<Project> getFullyFundedProjects();

    List<Project> getProjectsAbove80Percent();

}
