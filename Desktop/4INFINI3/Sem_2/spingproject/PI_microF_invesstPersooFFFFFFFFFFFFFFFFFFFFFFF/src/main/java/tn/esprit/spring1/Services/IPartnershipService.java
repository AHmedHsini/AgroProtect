package tn.esprit.spring1.Services;

import tn.esprit.spring1.entities.Partnership;

import java.util.Date;
import java.util.List;

public interface IPartnershipService {
    public Partnership addPartnership(Partnership Partnership);

    public Partnership updatePartnership(Partnership Partnership);

    public void deletePartnership(Long idPartnership);

    public Partnership getPartnershipById(Long idPartnership);

    public List<Partnership> getAllPartnership();

    public List<Partnership> addAllPartnership(List<Partnership> Partnerships);

    List<Partnership> getActivePartnerships();

    List<Partnership> getExpiredPartnerships();

    Double getAveragePartnershipDuration();

    Long countPartnershipsByPartner(Long idPartner);



}
