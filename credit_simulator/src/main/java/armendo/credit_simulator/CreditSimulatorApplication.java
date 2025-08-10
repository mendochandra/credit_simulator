package armendo.credit_simulator;

import armendo.credit_simulator.model.Credit;
import armendo.credit_simulator.service.CreditService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.time.Year;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class CreditSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditSimulatorApplication.class, args);
	}

	@Bean
	@Profile("!test")
	CommandLineRunner run(CreditService creditService) {
		return args -> {

			if (args.length > 0) {

				String filePath = args[0];
				try {
					String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));

					com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
					Credit credit = mapper.readValue(content, Credit.class);

					List<String> result = creditService.createCreditSimulation(credit);
					for (String s : result) {
						System.out.println(s);
					}
				} catch (Exception e) {
					System.out.println("Gagal membaca file: " + e.getMessage());
				}
			} else {

				Scanner scanner = new Scanner(System.in);
				boolean running = true;

				while (running) {
					System.out.println("\n==== Credit Simulation ====");
					System.out.println("1. Create Calculation Credit");
					System.out.println("2. Load Existing Calculation");
					System.out.println("3. Download sheet credit");
					System.out.println("4. Instruction");
					System.out.println("5. Exit");
					System.out.print("Choose option: ");

					int choice = Integer.parseInt(scanner.nextLine());

					switch (choice) {
						case 1 -> {

							Credit credit = new Credit();

							while (true) {
								System.out.print("Enter Vehichle Type: ");
								String vehichleType = scanner.nextLine();
								if (vehichleType == null || (!vehichleType.equalsIgnoreCase("Mobil") && !vehichleType.equalsIgnoreCase("Motor"))) {
									System.out.println("Vehicle Type must be 'Mobil' or 'Motor' ");
								}else {
									credit.setVehicleType(vehichleType);
									break;
								}
							}

							while (true) {
								System.out.print("Enter Vehicle Condition: ");
								String vehicleCondition = scanner.nextLine();
								if (vehicleCondition == null || (!vehicleCondition.equalsIgnoreCase("Baru") && !vehicleCondition.equalsIgnoreCase("Bekas"))) {
									System.out.println("Vehicle Condition must be 'Baru' or 'Bekas'");
								}else {
									credit.setVehicleCondition(vehicleCondition);
									break;
								}
							}

							while (true) {
								System.out.print("Enter Vehicle Year: ");
								int vehicleYear = Integer.parseInt(scanner.nextLine());
								if (String.valueOf(vehicleYear).length() != 4) {
									System.out.println("vehicleYear must be 4 digits.");
								}else {
									credit.setVehicleYear(vehicleYear);
									break;
								}
							}

							while (true) {
								System.out.print("Enter Total Loan Amount: ");
								int totalLoanAmount = Integer.parseInt(scanner.nextLine());
								if (totalLoanAmount <= 0) {
									System.out.println("totalLoanAmount must be positive");
								}else {
									credit.setTotalLoanAmount(totalLoanAmount);
									break;
								}
							}

							while (true) {
								System.out.print("Enter Loan Tenure: ");
								int loanTenure = Integer.parseInt(scanner.nextLine());
								if (loanTenure <= 0 || loanTenure > 6) {
									System.out.println("loanTenure must be between 1 and 6 years");
								}else {
									credit.setLoanTenure(loanTenure);
									break;
								}
							}

							while (true){
								System.out.print("Enter Down Payment: ");
								int downPayment = Integer.parseInt(scanner.nextLine());
								if (downPayment < 0) {
									System.out.println("downPayment cannot be negative.");
								}else {
									credit.setDownPayment(downPayment);
									break;
								}

							}

							if (credit.getVehicleCondition().equalsIgnoreCase("Baru")) {
								int currentYear = Year.now().getValue();
								if (credit.getVehicleYear() < (currentYear - 1)) {
									System.out.println("Vehicle with 'Baru' condition cannot have year less than currentYear");
									break;
								}
							}
							if (credit.getDownPayment() > credit.getTotalLoanAmount()) {
								System.out.println("Down payment must be less than total loan amount.");
								break;
							}


							double persenDp = ((double) credit.getDownPayment() / credit.getTotalLoanAmount()) * 100;
							System.out.println(persenDp + "%");
							if (credit.getVehicleCondition().equalsIgnoreCase("Baru")) {
								if (persenDp < 35) {
									System.out.println("For 'Baru' vehicles DP must be at least 35% of totalLoanAmount.");
									break;
								}
							} else {
								if (persenDp < 25) {
									System.out.println("For 'Bekas' vehicles DP must be at least 25% of totalLoanAmount.");
									break;
								}
							}

							List<String> result =  creditService.createCreditSimulation(credit);

							for (String s : result) {
								System.out.println(s);
							}
						}
						case 2 -> {
							List<String> result =  creditService.loadExisting();

							for (String s : result) {
								System.out.println(s);
							}
						}
						case 3 -> {
							String outputLink = creditService.createCalculationExcelAndGetLink();
							System.out.println(outputLink);
						}
						case 4 -> {
							System.out.println(creditService.instructionCredit());
						}
						case 5 -> {
							running = false;
							System.out.println("Goodbye!");
						}
						default -> System.out.println("Invalid choice.");
					}
				}
			}
		};
	}
}
