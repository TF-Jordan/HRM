package yowyob.comops.api.hrm.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure calculation engine implementing Cameroonian payroll legislation.
 * All methods are static and side-effect-free.
 */
public final class PayrollCalculationEngine {

    private PayrollCalculationEngine() {}

    // RM-01: CNPS
    private static final BigDecimal CNPS_TAUX_PV_SALARIE = new BigDecimal("0.042");
    private static final BigDecimal CNPS_PLAFOND = new BigDecimal("750000");

    // RM-02: IRPP
    private static final BigDecimal SEUIL_EXONERATION = new BigDecimal("62000");
    private static final BigDecimal ABATTEMENT_TAUX = new BigDecimal("0.70");
    private static final BigDecimal ABATTEMENT_SEUIL_BRUT = new BigDecimal("1333333");
    private static final BigDecimal ABATTEMENT_PLAFOND = new BigDecimal("400000");

    // IRPP brackets (monthly)
    private static final BigDecimal TRANCHE_1_LIMIT = new BigDecimal("166667");
    private static final BigDecimal TRANCHE_1_TAUX = new BigDecimal("0.10");
    private static final BigDecimal TRANCHE_2_LIMIT = new BigDecimal("250000");
    private static final BigDecimal TRANCHE_2_TAUX = new BigDecimal("0.15");
    private static final BigDecimal TRANCHE_3_LIMIT = new BigDecimal("416667");
    private static final BigDecimal TRANCHE_3_TAUX = new BigDecimal("0.25");
    private static final BigDecimal TRANCHE_4_TAUX = new BigDecimal("0.35");

    // RM-03: CAC
    private static final BigDecimal CAC_TAUX = new BigDecimal("0.10");

    // RM-07: CFC
    private static final BigDecimal CFC_TAUX = new BigDecimal("0.01");

    public static BigDecimal calculateCnpsEmploye(BigDecimal brut) {
        BigDecimal base = brut.min(CNPS_PLAFOND);
        return base.multiply(CNPS_TAUX_PV_SALARIE).setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateCnpsEmployeur(BigDecimal brut) {
        // PV employer: 4.2% + AF: 7% + AT: variable (default 1.75%)
        BigDecimal pvBase = brut.min(CNPS_PLAFOND);
        BigDecimal pvEmployeur = pvBase.multiply(CNPS_TAUX_PV_SALARIE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal af = brut.multiply(new BigDecimal("0.07")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal at = brut.multiply(new BigDecimal("0.0175")).setScale(0, RoundingMode.HALF_UP);
        return pvEmployeur.add(af).add(at);
    }

    public static BigDecimal calculateNetImposable(BigDecimal brut) {
        if (brut.compareTo(ABATTEMENT_SEUIL_BRUT) <= 0) {
            return brut.multiply(ABATTEMENT_TAUX).setScale(0, RoundingMode.HALF_UP);
        } else {
            return brut.subtract(ABATTEMENT_PLAFOND);
        }
    }

    public static BigDecimal calculateIrpp(BigDecimal brut) {
        if (brut.compareTo(SEUIL_EXONERATION) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal netImposable = calculateNetImposable(brut);
        BigDecimal irpp = BigDecimal.ZERO;
        BigDecimal remaining = netImposable;

        // Tranche 1: 0 - 166,667 at 10%
        BigDecimal tranche1 = remaining.min(TRANCHE_1_LIMIT);
        irpp = irpp.add(tranche1.multiply(TRANCHE_1_TAUX));
        remaining = remaining.subtract(tranche1);

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            // Tranche 2: 166,668 - 250,000 at 15%
            BigDecimal tranche2Width = TRANCHE_2_LIMIT.subtract(TRANCHE_1_LIMIT);
            BigDecimal tranche2 = remaining.min(tranche2Width);
            irpp = irpp.add(tranche2.multiply(TRANCHE_2_TAUX));
            remaining = remaining.subtract(tranche2);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            // Tranche 3: 250,001 - 416,667 at 25%
            BigDecimal tranche3Width = TRANCHE_3_LIMIT.subtract(TRANCHE_2_LIMIT);
            BigDecimal tranche3 = remaining.min(tranche3Width);
            irpp = irpp.add(tranche3.multiply(TRANCHE_3_TAUX));
            remaining = remaining.subtract(tranche3);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            // Tranche 4: beyond 416,667 at 35%
            irpp = irpp.add(remaining.multiply(TRANCHE_4_TAUX));
        }

        return irpp.setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateCac(BigDecimal irpp) {
        return irpp.multiply(CAC_TAUX).setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateCfc(BigDecimal brut) {
        if (brut.compareTo(SEUIL_EXONERATION) < 0) {
            return BigDecimal.ZERO;
        }
        return brut.multiply(CFC_TAUX).setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateRav(BigDecimal brut) {
        int brutInt = brut.intValue();
        if (brutInt <= 50000) return BigDecimal.ZERO;
        if (brutInt <= 100000) return new BigDecimal("750");
        if (brutInt <= 200000) return new BigDecimal("1950");
        if (brutInt <= 300000) return new BigDecimal("3250");
        if (brutInt <= 400000) return new BigDecimal("4550");
        if (brutInt <= 500000) return new BigDecimal("5850");
        if (brutInt <= 600000) return new BigDecimal("7150");
        if (brutInt <= 700000) return new BigDecimal("8450");
        if (brutInt <= 800000) return new BigDecimal("9750");
        if (brutInt <= 900000) return new BigDecimal("11050");
        if (brutInt <= 1000000) return new BigDecimal("12350");
        return new BigDecimal("13000");
    }

    public static BigDecimal calculateTdl(BigDecimal salaireBase) {
        int baseInt = salaireBase.intValue();
        BigDecimal tdlAnnuelle;
        if (baseInt < 62000) tdlAnnuelle = BigDecimal.ZERO;
        else if (baseInt <= 75000) tdlAnnuelle = new BigDecimal("3000");
        else if (baseInt <= 100000) tdlAnnuelle = new BigDecimal("6000");
        else if (baseInt <= 125000) tdlAnnuelle = new BigDecimal("9000");
        else if (baseInt <= 150000) tdlAnnuelle = new BigDecimal("12000");
        else if (baseInt <= 200000) tdlAnnuelle = new BigDecimal("15000");
        else if (baseInt <= 250000) tdlAnnuelle = new BigDecimal("18000");
        else if (baseInt <= 300000) tdlAnnuelle = new BigDecimal("24000");
        else if (baseInt <= 500000) tdlAnnuelle = new BigDecimal("27000");
        else tdlAnnuelle = new BigDecimal("30000");

        return tdlAnnuelle.divide(new BigDecimal("12"), 0, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateNetAPayer(BigDecimal brut, BigDecimal cnpsEmploye,
                                                 BigDecimal irpp, BigDecimal cac,
                                                 BigDecimal cfc, BigDecimal rav, BigDecimal tdl,
                                                 BigDecimal avancesDeduites) {
        return brut.subtract(cnpsEmploye).subtract(irpp).subtract(cac)
                .subtract(cfc).subtract(rav).subtract(tdl).subtract(avancesDeduites);
    }
}
