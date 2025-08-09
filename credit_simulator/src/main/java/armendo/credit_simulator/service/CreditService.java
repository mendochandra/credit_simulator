package armendo.credit_simulator.service;

import armendo.credit_simulator.model.Credit;

import java.io.IOException;
import java.util.List;

public interface CreditService {


    List<String> createCreditSimulation(Credit req);
    List<String>  loadExisting() throws IOException, InterruptedException;
    String createCalculationExcelAndGetLink() throws IOException;
    List<String> createCreditSimulationTxtFile(String filePath) throws Exception;
    String instructionCredit() throws Exception;

}
