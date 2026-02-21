package tn.esprit.agroprotect.microassurance.dto.mapper;

import org.mapstruct.*;
import tn.esprit.agroprotect.microassurance.dto.request.CreateSinistreRequest;
import tn.esprit.agroprotect.microassurance.dto.response.SinistreResponse;
import tn.esprit.agroprotect.microassurance.dto.response.SinistreSummaryResponse;
import tn.esprit.agroprotect.microassurance.entity.Sinistre;

import java.time.Instant;
import java.util.List;

/**
 * Mapper pour les sinistres
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SinistreMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "dateDeclaration", ignore = true)
    @Mapping(target = "motifRefus", ignore = true)
    @Mapping(target = "tauxRemboursement", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "indemnisation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Sinistre toEntity(CreateSinistreRequest request);

    SinistreResponse toResponse(Sinistre sinistre);

    SinistreSummaryResponse toSummaryResponse(Sinistre sinistre);

    List<SinistreResponse> toResponseList(List<Sinistre> sinistres);
    
    List<SinistreSummaryResponse> toSummaryResponseList(List<Sinistre> sinistres);


}