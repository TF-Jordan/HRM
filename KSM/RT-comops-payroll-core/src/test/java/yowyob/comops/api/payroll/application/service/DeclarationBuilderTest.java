package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;
import yowyob.comops.api.payroll.domain.model.DeclarationType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeclarationBuilderTest {

    private static DeclarationLineItem item(String matricule, String name, String cnps,
                                            String base, String ee, String er) {
        return new DeclarationLineItem(UUID.randomUUID(), matricule, name, cnps,
                new BigDecimal(base), new BigDecimal(ee), new BigDecimal(er));
    }

    @Test
    void aggregatesTotalsAcrossEmployees() {
        DeclarationDocument doc = DeclarationBuilder.build(DeclarationType.CNPS, "2026-10", List.of(
                item("EMP-1", "Alice", "CNPS-1", "400000", "16800", "51800"),
                item("EMP-2", "Bob", "CNPS-2", "300000", "12600", "38850")));

        assertThat(doc.employeeCount()).isEqualTo(2);
        assertThat(doc.totalGrossBase()).isEqualByComparingTo("700000");
        assertThat(doc.totalEmployee()).isEqualByComparingTo("29400");
        assertThat(doc.totalEmployer()).isEqualByComparingTo("90650");
        assertThat(doc.grandTotal()).isEqualByComparingTo("120050");
    }

    @Test
    void serialisesToSemicolonCsvWithHeaderAndTotals() {
        DeclarationDocument doc = DeclarationBuilder.build(DeclarationType.CNPS, "2026-10", List.of(
                item("EMP-1", "Alice", "CNPS-1", "400000", "16800", "51800")));
        String csv = DeclarationBuilder.toCsv(doc);
        String[] lines = csv.strip().split("\n");

        assertThat(lines[0]).isEqualTo("Matricule;Nom;NumeroCNPS;Base;PartSalariale;PartPatronale");
        assertThat(lines[1]).isEqualTo("EMP-1;Alice;CNPS-1;400000;16800;51800");
        assertThat(lines[2]).isEqualTo("TOTAL;1 employes;;400000;16800;51800");
    }

    @Test
    void escapesFieldsContainingSeparator() {
        DeclarationDocument doc = DeclarationBuilder.build(DeclarationType.DIPE, "2026-10", List.of(
                item("EMP-1", "Doe; John", "CNPS-1", "400000", "16800", "51800")));
        String csv = DeclarationBuilder.toCsv(doc);
        assertThat(csv).contains("\"Doe; John\"");
    }
}
