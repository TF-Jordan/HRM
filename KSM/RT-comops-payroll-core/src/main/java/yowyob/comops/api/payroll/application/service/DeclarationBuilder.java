package yowyob.comops.api.payroll.application.service;

import yowyob.comops.api.payroll.domain.model.DeclarationType;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * Aggregates declaration line items into a {@link DeclarationDocument} and serialises it to the
 * semicolon-separated CSV commonly accepted by Cameroonian administrations. Pure and stateless.
 */
public final class DeclarationBuilder {

    private DeclarationBuilder() {}

    public static DeclarationDocument build(DeclarationType type, String period,
                                            List<DeclarationLineItem> items) {
        BigDecimal totalBase = sum(items, DeclarationLineItem::grossBase);
        BigDecimal totalEmployee = sum(items, DeclarationLineItem::employeeContribution);
        BigDecimal totalEmployer = sum(items, DeclarationLineItem::employerContribution);
        return new DeclarationDocument(type, period, List.copyOf(items), totalBase, totalEmployee,
                totalEmployer, items.size());
    }

    /** Renders the declaration as a {@code ;}-separated CSV with a header and a totals row. */
    public static String toCsv(DeclarationDocument doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("Matricule;Nom;NumeroCNPS;Base;PartSalariale;PartPatronale\n");
        for (DeclarationLineItem item : doc.items()) {
            sb.append(csv(item.matricule())).append(';')
              .append(csv(item.employeeName())).append(';')
              .append(csv(item.socialSecurityNo())).append(';')
              .append(money(item.grossBase())).append(';')
              .append(money(item.employeeContribution())).append(';')
              .append(money(item.employerContribution())).append('\n');
        }
        sb.append("TOTAL;").append(doc.employeeCount()).append(" employes;;")
          .append(money(doc.totalGrossBase())).append(';')
          .append(money(doc.totalEmployee())).append(';')
          .append(money(doc.totalEmployer())).append('\n');
        return sb.toString();
    }

    private static BigDecimal sum(List<DeclarationLineItem> items,
                                  Function<DeclarationLineItem, BigDecimal> field) {
        return items.stream()
                .map(field).map(v -> v == null ? BigDecimal.ZERO : v)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String money(BigDecimal v) {
        return Rounding.money(v == null ? BigDecimal.ZERO : v).toPlainString();
    }

    /** Escapes a field for CSV: wraps in quotes when it contains a separator, quote or newline. */
    private static String csv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
