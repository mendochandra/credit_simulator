package armendo.credit_simulator;

import armendo.credit_simulator.model.Credit;
import armendo.credit_simulator.repository.CreditRepository;
import armendo.credit_simulator.service.CreditService;
import armendo.credit_simulator.service.CreditServiceImpl;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ActiveProfiles("test")
@SpringBootTest
class CreditSimulatorApplicationTests {

	@Mock
	private CreditRepository creditRepository;

	@Autowired
	private CreditService creditService;

    @Test
	void contextLoads() {
	}

	@Test
	void createSimulationTest() {
		Credit credit = new Credit();
		credit.setVehicleType("Mobil");
		credit.setVehicleCondition("Baru");
		credit.setVehicleYear(2025);
		credit.setTotalLoanAmount(1000000);
		credit.setLoanTenure(6);
		credit.setDownPayment(500000);

		List<String> result = creditService.createCreditSimulation(credit);

		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void loadExistingTest() {
		Credit credit = new Credit();
		credit.setVehicleType("Mobil");
		credit.setVehicleCondition("Baru");
		credit.setVehicleYear(2025);
		credit.setTotalLoanAmount(1000000);
		credit.setLoanTenure(6);
		credit.setDownPayment(500000);

		List<String> result = creditService.createCreditSimulation(credit);

		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void testLoadExisting() throws Exception {

		List<String> result = creditService.loadExisting();
		assertNotNull(result);
		assertFalse(result.isEmpty());

	}

	@Test
	void createCalculationExcelAndGetLinkTest() throws Exception {

		String result = creditService.createCalculationExcelAndGetLink();
		assertNotNull(result);
		assertFalse(result.isEmpty());

	}


}
