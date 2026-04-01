package AgroProtect.services;
import AgroProtect.entities.ApplicationStatus;
import AgroProtect.entities.CreditApplication;
import java.util.List;

public interface ICreditApplicationService {

    public CreditApplication addCreditApplication(CreditApplication CreditApplication);
    public void DeleteCreditApplication(long idCreditApplication);
    public CreditApplication getCreditApplicationById(long idCreditApplication);
    public List<CreditApplication> getAllCreditApplication();
    public List<CreditApplication> addAllCreditApplication(List<CreditApplication> CreditApplications);

}
