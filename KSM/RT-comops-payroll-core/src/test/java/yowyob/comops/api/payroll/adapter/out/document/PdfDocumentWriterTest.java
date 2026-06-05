package yowyob.comops.api.payroll.adapter.out.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfDocumentWriterTest {

    @Test
    void producesValidPdfContainingTheText() throws Exception {
        byte[] pdf = new PdfDocumentWriter()
                .title("BULLETIN DE PAIE")
                .heading("Employeur")
                .kv("Raison sociale", "ACME SARL")
                .separator()
                .row("Salaire de base", "400000", null, "400 000", false)
                .row("Net à payer", null, null, "332 066", true)
                .paragraph("Arrêté le présent bulletin à la somme de trois cent trente-deux mille "
                        + "soixante-six francs CFA.")
                .build();

        // valid PDF envelope
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(pdf.length).isGreaterThan(500);

        try (PDDocument doc = PDDocument.load(pdf)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("BULLETIN DE PAIE");
            assertThat(text).contains("ACME SARL");
            assertThat(text).contains("Net à payer");
            assertThat(text).contains("332 066");
        }
    }

    @Test
    void breaksToNewPageWhenContentOverflows() throws Exception {
        PdfDocumentWriter writer = new PdfDocumentWriter().title("LONG DOC");
        for (int i = 0; i < 120; i++) {
            writer.text("Ligne numéro " + i);
        }
        try (PDDocument doc = PDDocument.load(writer.build())) {
            assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(2);
        }
    }
}
