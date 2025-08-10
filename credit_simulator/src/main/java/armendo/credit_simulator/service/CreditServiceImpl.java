package armendo.credit_simulator.service;

import armendo.credit_simulator.model.Credit;
import armendo.credit_simulator.repository.CreditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CreditServiceImpl implements CreditService{

    private final CreditRepository creditRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Path exportDir = Paths.get("exports");

    private final ObjectMapper objectMapper;

    @Override
    public List<String> createCreditSimulation(Credit req) {

        creditRepository.save(req);
        return calculationCredit(req);
    }

    private List<String> calculationCredit(Credit req) {
        BigDecimal totalLoan = BigDecimal.valueOf(req.getTotalLoanAmount());
        BigDecimal dp = BigDecimal.valueOf(req.getDownPayment());
        BigDecimal principal = totalLoan.subtract(dp);

        BigDecimal baseRate = getBaseRateByVehicleType(req.getVehicleType());
        int totalMonths = req.getLoanTenure() * 12;

        List<String> results = new ArrayList<>();

        for (int year = 1; year <= req.getLoanTenure(); year++) {
            BigDecimal annualRate = computeAnnualRate(baseRate, year);
            int monthsRemaining = totalMonths - (year - 1) * 12;
            BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), 20, RoundingMode.HALF_UP);

            BigDecimal monthlyPayment = annuityMonthlyPayment(principal, monthlyRate, monthsRemaining);
            int monthsToApply = Math.min(12, monthsRemaining);
            for (int m = 0; m < monthsToApply; m++) {
                BigDecimal interest = principal.multiply(monthlyRate).setScale(20, RoundingMode.HALF_UP);
                BigDecimal principalPaid = monthlyPayment.subtract(interest);
                if (principalPaid.compareTo(BigDecimal.ZERO) < 0) principalPaid = BigDecimal.ZERO;
                principal = principal.subtract(principalPaid);
                if (principal.compareTo(BigDecimal.ZERO) < 0) {
                    principal = BigDecimal.ZERO;
                }
            }

            results.add(String.format(Locale.US, "tahun %d : Rp. %s/bln , Suku Bunga : %s%%",
                    year,
                    formatCurrency(monthlyPayment.setScale(2, RoundingMode.HALF_UP)),
                    annualRate.setScale(2, RoundingMode.HALF_UP).toPlainString()));

            if (principal.compareTo(BigDecimal.ZERO) == 0) break;
        }
        return results;
    }

    private static String formatCurrency(BigDecimal amount) {
        return String.format("%,.2f", amount);
    }

    private BigDecimal annuityMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int months) {
        if (months <= 0) return BigDecimal.ZERO;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 20, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusI = BigDecimal.ONE.add(monthlyRate);

        BigDecimal denom = BigDecimal.ONE.subtract(pow(onePlusI, -months));
        if (denom.compareTo(BigDecimal.ZERO) == 0) return principal.divide(BigDecimal.valueOf(months), 20, RoundingMode.HALF_UP);
        return principal.multiply(monthlyRate).divide(denom, 20, RoundingMode.HALF_UP);
    }

    private BigDecimal pow(BigDecimal base, int negExp) {
        if (negExp >= 0) throw new IllegalArgumentException("pow expects negative exponent here");
        double dbl = Math.pow(base.doubleValue(), negExp);
        return BigDecimal.valueOf(dbl);
    }

    private BigDecimal computeAnnualRate(BigDecimal baseRate, int year) {
        BigDecimal r = baseRate;
        for (int k = 2; k <= year; k++) {
            if (k % 2 == 0) r = r.add(BigDecimal.valueOf(0.1));
            else r = r.add(BigDecimal.valueOf(0.5));
        }
        return r;
    }

    private BigDecimal getBaseRateByVehicleType(String vehicleType) {
        if (vehicleType.equalsIgnoreCase("Mobil")){
            return BigDecimal.valueOf(8.0);
        }else {
            return BigDecimal.valueOf(9.0);

        }
    }

    @Override
    public List<String>  loadExisting() throws IOException, InterruptedException {
        String url = "https://kmr60.wiremockapi.cloud/v3/9108b1da-beec-409e-ae14-e8091955666c";
        JsonObject hitThirdParties = hitThirdPartyApi(url);

        Credit req = new Credit();
        req.setVehicleType(hitThirdParties.getString("vehicleType"));
        req.setVehicleCondition(hitThirdParties.getString("vehicleCondition"));
        req.setVehicleYear(Integer.valueOf(hitThirdParties.getString("vehicleYear")));
        req.setTotalLoanAmount(Integer.valueOf(hitThirdParties.getString("totalLoanAmount")));
        req.setLoanTenure(Integer.valueOf(hitThirdParties.getString("loanTenure")));
        req.setDownPayment(Integer.valueOf(hitThirdParties.getString("downPayment")));
        return calculationCredit(req);
    }

    public JsonObject hitThirdPartyApi(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new JsonObject(response.body());
    }

    @Override
    public String createCalculationExcelAndGetLink() throws IOException {
        List<Credit> allCredits = creditRepository.findAll();

        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
        String fileName = "all_calculations_" + System.currentTimeMillis() + ".xlsx";
        Path filePath = exportDir.resolve(fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("All Calculations");

            int rowNum = 0;
            // Header
            Row header = sheet.createRow(rowNum++);
            header.createCell(0).setCellValue("Vehicle Type");
            header.createCell(1).setCellValue("Vehicle Condition");
            header.createCell(2).setCellValue("Vehicle Year");
            header.createCell(3).setCellValue("Total Loan Amount");
            header.createCell(4).setCellValue("Loan Tenure");
            header.createCell(5).setCellValue("Down Payment");
            header.createCell(6).setCellValue("Calculation Results");

            // Data rows
            for (Credit credit : allCredits) {
                List<String> calculationResults = createCreditSimulation(credit); // kalkulasi

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(credit.getVehicleType());
                row.createCell(1).setCellValue(credit.getVehicleCondition());
                row.createCell(2).setCellValue(credit.getVehicleYear());
                row.createCell(3).setCellValue(credit.getTotalLoanAmount().doubleValue());
                row.createCell(4).setCellValue(credit.getLoanTenure());
                row.createCell(5).setCellValue(credit.getDownPayment().doubleValue());
                row.createCell(6).setCellValue(String.join("\n", calculationResults)); // gabungkan perhitungan jadi 1 cell
            }

            // Autosize kolom
            for (int i = 0; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // Simpan file
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }

        if (allCredits.isEmpty()) {
            return "Please create credit simulation first";
        } else {
            return uploadToGitHub(filePath, fileName);
        }
    }

    private String uploadToGitHub(Path filePath, String fileName) throws IOException {

        String githubToken = System.getenv("GITHUB_TOKEN"); // simpan token di env
        String githubRepo = "mendochandra/credit_simulator";
        String branch = "main";

        byte[] fileBytes = Files.readAllBytes(filePath);
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);

        String jsonPayload = String.format(
                "{ \"message\": \"Add %s\", \"content\": \"%s\", \"branch\": \"%s\" }",
                fileName, base64Content, branch
        );

        URL url = new URL("https://api.github.com/repos/" + githubRepo + "/contents/exports/" + fileName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "token " + githubToken);
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 201 || responseCode == 200) {
            return "https://raw.githubusercontent.com/" + githubRepo + "/" + branch + "/exports/" + fileName;
        } else {
            throw new IOException("Failed to upload file to GitHub. Response code: " + responseCode);
        }
    }

    @Override
    public String instructionCredit() throws Exception {
        String instruction = """
        ==== Instruction ====
        1. Create Calculation Credit
           - Membuat simulasi kredit baru dengan memasukkan jenis kendaraan, kondisi, tahun, total pinjaman, tenor, dan DP.
        2. Load Existing Calculation
           - Memuat data simulasi kredit dari web service, lalu menghitung hasilnya otomatis.
        3. Download Sheet Credit
           - Mengunduh hasil simulasi kredit dalam format Excel yang berasal dari semua create simulation credit.
        """;
        return instruction;
    }
}
