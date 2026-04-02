package tn.esprit.spring1.Services;

import tn.esprit.spring1.entities.Partner;
import tn.esprit.spring1.entities.PartnerType;

import java.util.List;

public interface IPartnerService {
    public Partner addPartner(Partner Partner);

    public Partner updatePartner(Partner Partner);

    public void deletePartner(Long idPartner);

    public Partner getPartnerById(Long idPartner);

    public List<Partner> getAllPartner();

    public List<Partner> addAllPartner(List<Partner> Partners);

    List<Partner> getPartnersByRegion(String region);

    List<Partner> getPartnersByType(PartnerType type);

    List<Partner> getMostInvolvedPartners();

    Long getActivePartnershipsCount(Long idPartner);

}
