package tn.esprit.spring1.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.QuoteServiceImpl;
import tn.esprit.spring1.dto.AIResult;
import tn.esprit.spring1.entities.Project;
import tn.esprit.spring1.repositories.ProjectRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AIDashboardController {

    private final ProjectRepository projectRepository;
    private final QuoteServiceImpl quoteService;

    @GetMapping("/dashboard/{projectId}")
    public String dashboard(Model model, @PathVariable Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        AIResult ai = quoteService.analyzeProject(project);

        model.addAttribute("project", project);
        model.addAttribute("ai", ai);

        return "ai-dashboard";
    }
}