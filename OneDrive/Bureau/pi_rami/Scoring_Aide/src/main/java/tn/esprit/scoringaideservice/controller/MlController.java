package tn.esprit.scoringaideservice.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.scoringaideservice.service.MlService;
import tn.esprit.scoringaideservice.dto.MlRequest;

@RestController
@RequestMapping("/ml")
public class MlController {

    private final MlService mlService;

    public MlController(MlService mlService) {
        this.mlService = mlService;
    }

    @PostMapping("/predict")
    public Double predict(@RequestBody MlRequest input) {

        return mlService.getPrediction(
                input.getAgronomique(),
                input.getClimatique(),
                input.getProductivite(),
                input.getStabilite(),
                input.getMarket()
        );
    }
}