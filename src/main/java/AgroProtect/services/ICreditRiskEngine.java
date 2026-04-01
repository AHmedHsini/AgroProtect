package AgroProtect.services;

import AgroProtect.entities.CreditApplication;

public interface ICreditRiskEngine {
    void evaluate(CreditApplication application);
}