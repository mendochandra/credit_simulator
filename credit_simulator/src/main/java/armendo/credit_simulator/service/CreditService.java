package armendo.credit_simulator.service;

import armendo.credit_simulator.model.Credit;

import java.util.List;

public interface CreditService {


    List<String> createCreditSimulation(Credit req);
    List<String>  loadExisting(Credit credit);

}
