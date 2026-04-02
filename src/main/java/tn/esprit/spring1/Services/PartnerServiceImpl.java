package tn.esprit.spring1.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.Partner;
import tn.esprit.spring1.entities.PartnerType;
import tn.esprit.spring1.repositories.PartnerRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PartnerServiceImpl implements IPartnerService{

    PartnerRepository chrep;

    @Override
    public Partner addPartner(Partner Partner) {
        return chrep.save(Partner);
    }

    @Override
    public Partner updatePartner(Partner Partner) {
        return chrep.save(Partner);
    }

    @Override
    public void deletePartner(Long idPartner) {
        chrep.deleteById(idPartner);
    }

    @Override
    public Partner getPartnerById(Long idPartner) {
        //return chrep.findById(idPartner).get();
        return chrep.findById(idPartner).orElse(null);
    }

    @Override
    public List<Partner> getAllPartner() {
        return (List<Partner>) chrep.findAll();
        //return List.of((Partner) chrep.findAll());
    }

    @Override
    public List<Partner> addAllPartner(List<Partner> Partners) {
        return (List<Partner>) chrep.saveAll(Partners);
        //return List.of((Partner) chrep.saveAll(Partners));
    }
    @Override
    public List<Partner> getPartnersByRegion(String region) {
        return chrep.findByRegion(region);
    }

    @Override
    public List<Partner> getPartnersByType(PartnerType type) {
        return chrep.findByPartnerType(type);
    }

    @Override
    public List<Partner> getMostInvolvedPartners() {
        return chrep.findMostInvolvedPartners();
    }

    @Override
    public Long getActivePartnershipsCount(Long idPartner) {
        return chrep.countActivePartnerships(idPartner);
    }

}
