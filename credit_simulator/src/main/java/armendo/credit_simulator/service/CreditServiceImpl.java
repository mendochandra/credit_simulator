package armendo.credit_simulator.service;

import armendo.credit_simulator.model.Credit;
import armendo.credit_simulator.repository.CreditRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CreditServiceImpl implements CreditService{

    private final CreditRepository creditRepository;

    @Override
    public List<String> createCreditSimulation(Credit req) {

        creditRepository.save(req);

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
    public List<String>  loadExisting(Credit credit) {
//        https://kmr60.wiremockapi.cloud/v3/9108b1da-beec-409e-ae14-e8091955666c
        return null;
    }
}
