package yowyob.comops.api.payroll.adapter.out.document;

import yowyob.comops.api.payroll.application.port.out.EmployerInfo;
import yowyob.comops.api.payroll.application.service.AmountToFrenchWords;
import yowyob.comops.api.payroll.domain.model.DocumentSeal;
import yowyob.comops.api.payroll.domain.model.FinalSettlement;
import yowyob.comops.api.payroll.domain.model.PayslipLine;
import yowyob.comops.api.payroll.domain.model.PayslipLineType;
import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Renders payroll documents (payslip, final settlement, work certificate) to PDF conforming to
 * Cameroon labour code (Art. 68-69 for payslips, Art. 39 for STC). Builds the deterministic
 * <em>canonical content</em> that gets signed. The PDF prints the employer header, the document
 * body, and a seal block (algorithm, verification code, content hash) so the electronic signature
 * is visible and verifiable. Pure (no Spring).
 */
public final class PayrollPdfRenderer {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        MONEY = new DecimalFormat("#,##0", symbols);
    }

    private static final Map<TerminationReason, String> TERMINATION_LABELS = Map.of(
            TerminationReason.RESIGNATION, "Démission",
            TerminationReason.DISMISSAL, "Licenciement",
            TerminationReason.DISMISSAL_GROSS_MISCONDUCT, "Licenciement pour faute lourde",
            TerminationReason.END_OF_CONTRACT, "Fin de contrat",
            TerminationReason.RETIREMENT, "Départ à la retraite",
            TerminationReason.MUTUAL_AGREEMENT, "Rupture conventionnelle",
            TerminationReason.DEATH, "Décès en service"
    );

    private PayrollPdfRenderer() {}

    // ----------------------------------------------------------------- payslip

    public static String payslipCanonical(PayslipView v, EmployerInfo employer) {
        StringBuilder sb = new StringBuilder("PAYSLIP|v1");
        sb.append('|').append(employer == null ? "" : employer.legalName());
        sb.append('|').append(v.periode());
        sb.append('|').append(nullSafe(v.matricule()));
        sb.append('|').append(nullSafe(v.employeeName()));
        sb.append('|').append(money(v.brut()));
        sb.append('|').append(money(v.totalDeductions()));
        sb.append('|').append(money(v.net()));
        for (PayslipLine line : v.lines()) {
            sb.append('|').append(line.libelle()).append('=').append(money(line.montant()));
        }
        return sb.toString();
    }

    public static byte[] renderPayslip(PayslipView v, EmployerInfo employer, DocumentSeal seal) {
        PdfDocumentWriter w = new PdfDocumentWriter();
        w.title("BULLETIN DE PAIE");
        w.text("Période : " + v.periode());
        if (employer != null && employer.conventionCollective() != null) {
            w.text("Convention collective : " + employer.conventionCollective());
        }
        employerHeader(w, employer);

        w.heading("Salarié");
        w.kv("Nom", v.employeeName());
        w.kv("Matricule", v.matricule());
        w.kv("N° CNPS", v.socialSecurityNo());
        w.kv("Catégorie / Échelon", nullSafe(v.categorie()) + " / " + nullSafe(v.echelon()));
        if (v.hireDate() != null) {
            w.kv("Date d'embauche", v.hireDate().format(DATE));
        }

        // --- Earnings and employee deductions ---
        w.heading("Détail des éléments de paie");
        w.row("Libellé", "Base", "Taux", "Montant", true);
        w.separator();
        for (PayslipLine line : v.lines()) {
            if (line.type() == PayslipLineType.EARNING || line.type() == PayslipLineType.DEDUCTION) {
                w.row(line.libelle(), line.base() == null ? null : money(line.base()),
                        line.taux() == null ? null : ratePct(line.taux()), money(line.montant()), false);
            }
        }
        w.separator();
        w.row("SALAIRE BRUT", null, null, money(v.brut()), true);
        w.row("Total retenues salariales", null, null, money(v.totalDeductions()), false);
        if (v.incomeTax() != null && v.incomeTax().signum() > 0) {
            w.row("  dont IRPP", null, null, money(v.incomeTax()), false);
        }
        w.separator();
        w.row("NET À PAYER", null, null, money(v.net()), true);

        // --- Employer charges ---
        boolean hasEmployerLines = v.lines().stream()
                .anyMatch(l -> l.type() == PayslipLineType.EMPLOYER_INFO);
        if (hasEmployerLines || (v.employerCharges() != null && v.employerCharges().signum() > 0)) {
            w.heading("Charges patronales");
            for (PayslipLine line : v.lines()) {
                if (line.type() == PayslipLineType.EMPLOYER_INFO) {
                    w.row(line.libelle(), line.base() == null ? null : money(line.base()),
                            line.taux() == null ? null : ratePct(line.taux()), money(line.montant()), false);
                }
            }
            w.separator();
            w.row("TOTAL CHARGES PATRONALES", null, null, money(v.employerCharges()), true);
        }

        // --- Annual cumulative ---
        if (v.cumulGross() != null || v.cumulNet() != null) {
            w.heading("Cumuls annuels");
            w.kv("Brut cumulé", money(v.cumulGross()));
            w.kv("Net cumulé", money(v.cumulNet()));
        }

        // --- Payment info ---
        if (v.paymentChannel() != null) {
            w.heading("Mode de paiement");
            w.kv("Canal", v.paymentChannel());
            if (v.accountRef() != null) {
                w.kv("Référence", v.accountRef());
            }
        }

        w.spacer(6);
        w.paragraph("Arrêté le présent bulletin à la somme nette de "
                + AmountToFrenchWords.moneyInWords(v.net(), "francs CFA") + ".");

        // --- Location and date ---
        String city = employer != null && employer.city() != null ? employer.city() : "Douala";
        w.spacer(6);
        w.text("Fait à " + city + ", le " + LocalDate.now().format(DATE));

        sealFooter(w, seal);
        return w.build();
    }

    // -------------------------------------------------------- final settlement

    public static String settlementCanonical(FinalSettlement s, String employeeName) {
        return "STC|v1|" + s.periode() + "|" + s.employeeId() + "|" + nullSafe(employeeName)
                + "|reason=" + s.reason() + "|gross=" + money(s.grossSettlement())
                + "|net=" + money(s.netSettlement());
    }

    public static byte[] renderFinalSettlement(FinalSettlement s, String employeeName,
                                               EmployerInfo employer, DocumentSeal seal) {
        PdfDocumentWriter w = new PdfDocumentWriter();
        w.title("SOLDE DE TOUT COMPTE");
        employerHeader(w, employer);

        w.heading("Salarié");
        w.kv("Nom", employeeName);
        w.kv("Date de départ", s.departureDate() == null ? "" : s.departureDate().format(DATE));
        w.kv("Motif", terminationLabel(s.reason()));
        w.kv("Ancienneté", s.seniorityYears() + " an(s)");

        w.heading("Décompte");
        w.row("Salaire (prorata)", null, null, money(s.proratedSalary()), false);
        w.row("Indemnité compensatrice de congés", null, null, money(s.leaveCompensation()), false);
        w.row("Indemnité de préavis", null, null, money(s.noticeIndemnity()), false);
        w.row("Indemnité de licenciement", null, null, money(s.severanceIndemnity()), false);
        w.row("Gratification", null, null, money(s.gratification()), false);
        w.separator();
        w.row("TOTAL BRUT", null, null, money(s.grossSettlement()), true);
        w.row("Apurement prêts", null, null, money(s.loanDeducted()), false);
        w.row("NET À PAYER", null, null, money(s.netSettlement()), true);

        w.spacer(6);
        w.paragraph("Arrêté le présent solde de tout compte à la somme nette de "
                + AmountToFrenchWords.moneyInWords(s.netSettlement(), "francs CFA") + ".");

        // Quittance clause (Art. 39 Code du travail camerounais)
        w.spacer(6);
        w.paragraph("Le présent reçu pour solde de tout compte peut être dénoncé dans les deux mois "
                + "de sa signature, par lettre recommandée. Passé ce délai, il devient libératoire pour "
                + "l'employeur pour les sommes qui y sont mentionnées.");

        // Signature blocks
        w.spacer(12);
        String city = employer != null && employer.city() != null ? employer.city() : "Douala";
        w.text("Fait à " + city + ", le " + LocalDate.now().format(DATE));
        w.spacer(8);
        w.kv("L'employeur", "Le salarié");
        w.spacer(20);
        w.kv("Signature :", "Signature (précédée de la mention \"Lu et approuvé\") :");

        sealFooter(w, seal);
        return w.build();
    }

    // ------------------------------------------------------- work certificate

    public static String certificateCanonical(WorkCertificateView v, EmployerInfo employer) {
        return "CERT|v1|" + (employer == null ? "" : employer.legalName()) + "|" + v.employeeId()
                + "|" + nullSafe(v.employeeName()) + "|from=" + v.hireDate()
                + "|to=" + (v.departureDate() == null ? "présent" : v.departureDate());
    }

    public static byte[] renderWorkCertificate(WorkCertificateView v, EmployerInfo employer,
                                               DocumentSeal seal) {
        PdfDocumentWriter w = new PdfDocumentWriter();
        w.title("CERTIFICAT DE TRAVAIL");
        employerHeader(w, employer);
        w.spacer(8);

        String employerName = employer == null ? "L'employeur" : employer.legalName();
        String ceoName = employer != null && employer.ceoName() != null ? employer.ceoName() : "";
        String period = v.hireDate().format(DATE) + " au "
                + (v.departureDate() == null ? "ce jour" : v.departureDate().format(DATE));
        w.paragraph("Nous, soussignés " + employerName
                + (ceoName.isEmpty() ? "" : ", représentée par " + ceoName)
                + ", certifions que "
                + nullSafe(v.employeeName()) + " (matricule " + nullSafe(v.matricule()) + ") "
                + "a été employé(e) au sein de notre entreprise du " + period
                + " en qualité de " + nullSafe(v.position()) + ".");

        w.spacer(6);
        w.paragraph(nullSafe(v.employeeName()) + " quitte notre entreprise libre de tout engagement.");

        w.spacer(6);
        w.paragraph("En foi de quoi le présent certificat lui est délivré pour servir et valoir "
                + "ce que de droit.");

        // Location, date, and signature
        w.spacer(12);
        String city = employer != null && employer.city() != null ? employer.city() : "Douala";
        w.text("Fait à " + city + ", le " + LocalDate.now().format(DATE));
        w.spacer(20);
        if (!ceoName.isEmpty()) {
            w.text(ceoName);
        }
        w.text("L'employeur");

        sealFooter(w, seal);
        return w.build();
    }

    // ------------------------------------------------------------- shared bits

    private static void employerHeader(PdfDocumentWriter w, EmployerInfo employer) {
        w.heading("Employeur");
        if (employer == null) {
            w.text("—");
            return;
        }
        w.kv("Raison sociale", employer.legalName());
        if (employer.legalForm() != null) {
            String formLine = employer.legalForm();
            if (employer.capitalShare() != null) {
                formLine += " au capital de " + money(employer.capitalShare()) + " FCFA";
            }
            w.kv("Forme juridique", formLine);
        }
        if (employer.address() != null) {
            String addr = employer.address();
            if (employer.postalCode() != null) {
                addr += " - " + employer.postalCode();
            }
            if (employer.city() != null) {
                addr += " " + employer.city();
            }
            w.kv("Adresse", addr);
        }
        if (employer.phone() != null) {
            w.kv("Tél", employer.phone());
        }
        if (employer.email() != null) {
            w.kv("Email", employer.email());
        }
        if (employer.registrationNumber() != null) {
            w.kv("RCCM", employer.registrationNumber());
        }
        if (employer.taxNumber() != null) {
            w.kv("N° contribuable", employer.taxNumber());
        }
        if (employer.cnpsEmployerNumber() != null) {
            w.kv("N° CNPS employeur", employer.cnpsEmployerNumber());
        }
    }

    private static void sealFooter(PdfDocumentWriter w, DocumentSeal seal) {
        if (seal == null) {
            return;
        }
        w.spacer(12);
        w.separator();
        w.text("Document signé électroniquement (" + seal.algorithm() + ")");
        w.text("Code de vérification : " + seal.verificationCode());
        w.text("Empreinte SHA-256 : " + seal.contentHashHex());
        if (seal.signedAt() != null) {
            w.text("Signé le : " + seal.signedAt());
        }
    }

    private static String terminationLabel(TerminationReason reason) {
        if (reason == null) return "";
        return TERMINATION_LABELS.getOrDefault(reason, reason.name());
    }

    private static String money(BigDecimal v) {
        return MONEY.format(v == null ? BigDecimal.ZERO : v);
    }

    private static String ratePct(BigDecimal rate) {
        return rate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    /** Data needed to render a payslip (assembled by the service). */
    public record PayslipView(
            String periode, String employeeName, String matricule, String socialSecurityNo,
            String categorie, String echelon, LocalDate hireDate, BigDecimal brut,
            BigDecimal totalDeductions, BigDecimal incomeTax, BigDecimal employerCharges, BigDecimal net,
            BigDecimal cumulGross, BigDecimal cumulNet, List<PayslipLine> lines,
            String paymentChannel, String accountRef) {
    }

    /** Data needed to render a work certificate. */
    public record WorkCertificateView(
            java.util.UUID employeeId, String employeeName, String matricule, String position,
            LocalDate hireDate, LocalDate departureDate) {
    }
}
