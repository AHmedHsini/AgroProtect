package AgroProtect.services;

import AgroProtect.entities.AgriculturalCredit;
import AgroProtect.entities.CreditStatus;

import java.util.List;

public interface IAgriculturalCreditService {

    AgriculturalCredit createCreditFromApplication(Long applicationId, double approvedAmount);
    AgriculturalCredit updateCreditStatus(Long id, CreditStatus newStatus);


    AgriculturalCredit getAgriculturalCreditById(Long id);

    List<AgriculturalCredit> getAllAgriculturalCredit();
}