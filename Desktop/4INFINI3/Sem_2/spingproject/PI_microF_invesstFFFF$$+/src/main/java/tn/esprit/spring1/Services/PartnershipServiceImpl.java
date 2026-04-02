package tn.esprit.spring1.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.Partnership;
import tn.esprit.spring1.repositories.PartnershipRepository;
import tn.esprit.spring1.repositories.PartnershipRepository;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class PartnershipServiceImpl implements IPartnershipService{

    PartnershipRepository chrep;
    private PartnershipRepository PartnershipRepository;


    @Override
    public Partnership addPartnership(Partnership Partnership) {
        return chrep.save(Partnership);
    }

    @Override
    public Partnership updatePartnership(Partnership Partnership) {
        return chrep.save(Partnership);
    }

    @Override
    public void deletePartnership(Long idPartnership) {
        chrep.deleteById(idPartnership);
    }

    @Override
    public Partnership getPartnershipById(Long idPartnership) {
        //return chrep.findById(idPartnership).get();
        return chrep.findById(idPartnership).orElse(null);
    }

    @Override
    public List<Partnership> getAllPartnership() {
        return (List<Partnership>) chrep.findAll();
        //return List.of((Partnership) chrep.findAll());
    }

    @Override
    public List<Partnership> addAllPartnership(List<Partnership> Partnerships) {
        return (List<Partnership>) chrep.saveAll(Partnerships);
        //return List.of((Partnership) chrep.saveAll(Partnerships));
    }

    @Override
    public List<Partnership> getActivePartnerships() {
        Date today = new Date();
        return chrep.findByStartDateBeforeAndEndDateAfter(today, today);
    }

    @Override
    public List<Partnership> getExpiredPartnerships() {
        Date today = new Date();
        return chrep.findByEndDateBefore(today);
    }

    @Override
    public Double getAveragePartnershipDuration() {
        return chrep.getAverageDuration();
    }

    @Override
    public Long countPartnershipsByPartner(Long idPartner) {
        return chrep.countByPartner(idPartner);
    }

}
