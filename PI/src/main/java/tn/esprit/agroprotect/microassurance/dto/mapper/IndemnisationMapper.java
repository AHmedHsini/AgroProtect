package tn.esprit.agroprotect.microassurance.dto.mapper;

import org.mapstruct.*;
import tn.esprit.agroprotect.microassurance.dto.response.IndemnisationResponse;
import tn.esprit.agroprotect.microassurance.dto.response.IndemnisationSummaryResponse;
import tn.esprit.agroprotect.microassurance.entity.Indemnisation;

import java.util.List;

/**
 * Mapper pour les indemnisations
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IndemnisationMapper {

    @Mapping(target = "sinistreId", source = "sinistre.id")
    IndemnisationResponse toResponse(Indemnisation indemnisation);

    IndemnisationSummaryResponse toSummaryResponse(Indemnisation indemnisation);

    List<IndemnisationResponse> toResponseList(List<Indemnisation> indemnisations);
    
    List<IndemnisationSummaryResponse> toSummaryResponseList(List<Indemnisation> indemnisations);
}