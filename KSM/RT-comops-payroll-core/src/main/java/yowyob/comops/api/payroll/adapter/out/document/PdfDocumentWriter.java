package yowyob.comops.api.payroll.adapter.out.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A small, flowing-layout PDF builder over Apache PDFBox: a downward y-cursor with helpers for
 * titles, key/value lines, table rows, separators, wrapped paragraphs, filled bands, colored
 * text and section labels, plus automatic page breaks. Pure (no Spring); produces the document
 * bytes via {@link #build()}.
 *
 * Text is rendered with the Standard-14 Helvetica fonts (WinAnsi), which cover French accents;
 * any character outside that encoding is sanitised so rendering never fails.
 *
 * The colour palette mirrors the frontend design tokens so a payslip rendered to PDF reads
 * like its on-screen preview (ink, ink-3, line, bg-soft …).
 */
public final class PdfDocumentWriter {

    private static final PDFont REGULAR = PDType1Font.HELVETICA;
    private static final PDFont BOLD = PDType1Font.HELVETICA_BOLD;
    private static final PDFont ITALIC = PDType1Font.HELVETICA_OBLIQUE;
    private static final float MARGIN = 50f;
    private static final float TOP = PDRectangle.A4.getHeight() - MARGIN;
    private static final float BOTTOM = MARGIN;
    private static final float WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;

    // Design tokens — same RGB triplets as the frontend Tailwind palette.
    public static final Color INK = new Color(0x1A150E);
    public static final Color INK_2 = new Color(0x3F3326);
    public static final Color INK_3 = new Color(0x756449);
    public static final Color BG_SOFT = new Color(0xF5F1EA);
    public static final Color LINE = new Color(0xE5E0D5);
    public static final Color LINE_SOFT = new Color(0xEFE9DD);
    public static final Color WHITE = Color.WHITE;

    private final PDDocument document = new PDDocument();
    private PDPage page;
    private PDPageContentStream cs;
    private float y;

    public PdfDocumentWriter() {
        newPage();
    }

    private void newPage() {
        closeStream();
        page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        try {
            cs = new PDPageContentStream(document, page);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        y = TOP;
    }

    private void ensure(float needed) {
        if (y - needed < BOTTOM) {
            newPage();
        }
    }

    public PdfDocumentWriter title(String text) {
        ensure(30);
        float width = stringWidth(BOLD, 16, text);
        drawText(BOLD, 16, MARGIN + (WIDTH - width) / 2, y, text, INK);
        y -= 30;
        return this;
    }

    public PdfDocumentWriter heading(String text) {
        ensure(20);
        y -= 6;
        drawText(BOLD, 11, MARGIN, y, text, INK);
        y -= 16;
        return this;
    }

    public PdfDocumentWriter text(String text) {
        ensure(14);
        drawText(REGULAR, 9, MARGIN, y, text, INK_2);
        y -= 13;
        return this;
    }

    /** A label (bold) + value on one line. */
    public PdfDocumentWriter kv(String label, String value) {
        ensure(14);
        drawText(BOLD, 9, MARGIN, y, label, INK);
        drawText(REGULAR, 9, MARGIN + 150, y, value == null ? "" : value, INK_2);
        y -= 14;
        return this;
    }

    /** A label/value row tinted like the frontend preview (label in INK_3, value in INK). */
    public PdfDocumentWriter kvSoft(String label, String value) {
        ensure(13);
        drawText(REGULAR, 9, MARGIN, y, label, INK_3);
        drawText(REGULAR, 9, MARGIN + 150, y, value == null ? "" : value, INK);
        y -= 13;
        return this;
    }

    /** A table row: each cell at its x; the last cell (amount) is right-aligned to the page edge. */
    public PdfDocumentWriter row(String label, String base, String rate, String amount, boolean bold) {
        ensure(15);
        PDFont font = bold ? BOLD : REGULAR;
        Color color = bold ? INK : INK_2;
        drawText(font, 9, MARGIN, y, label, color);
        if (base != null) {
            drawTextRight(REGULAR, 9, MARGIN + 320, y, base, INK_3);
        }
        if (rate != null) {
            drawTextRight(REGULAR, 9, MARGIN + 390, y, rate, INK_3);
        }
        drawTextRight(font, 9, MARGIN + WIDTH, y, amount == null ? "" : amount, color);
        y -= 14;
        return this;
    }

    /**
     * A bold subtotal row drawn on a soft background band — matches the
     * "Salaire brut" / "Total charges patronales" rows in the on-screen preview.
     */
    public PdfDocumentWriter subtotalRow(String label, String amount) {
        ensure(20);
        fillBand(BG_SOFT, 18);
        drawText(BOLD, 10, MARGIN + 6, y - 13, label, INK);
        drawTextRight(BOLD, 10, MARGIN + WIDTH - 6, y - 13, amount == null ? "" : amount, INK);
        y -= 22;
        return this;
    }

    /**
     * Highlighted "Net à payer" band: full-width ink-coloured rectangle with white text
     * (label on the left, amount right-aligned in a larger size).
     */
    public PdfDocumentWriter netToPayBand(String label, String amount) {
        ensure(34);
        fillBand(INK, 30);
        drawText(BOLD, 12, MARGIN + 10, y - 19, label, WHITE);
        drawTextRight(BOLD, 18, MARGIN + WIDTH - 10, y - 21, amount == null ? "" : amount, WHITE);
        y -= 36;
        return this;
    }

    /**
     * Tiny uppercase, letter-spaced section label like the
     * "EMPLOYEUR" / "SALARIÉ" labels in the preview.
     */
    public PdfDocumentWriter sectionLabel(String text) {
        ensure(14);
        y -= 4;
        drawText(BOLD, 8, MARGIN, y, text == null ? "" : text.toUpperCase(), INK_3);
        y -= 12;
        return this;
    }

    /**
     * Table column header line: small uppercase grey labels and a 2px ink bottom border —
     * matches the table head in {@code PayslipPreview}.
     */
    public PdfDocumentWriter tableHeader(String labelCol, String baseCol, String rateCol, String amountCol) {
        ensure(20);
        drawText(BOLD, 8, MARGIN, y, labelCol == null ? "" : labelCol.toUpperCase(), INK_3);
        if (baseCol != null) {
            drawTextRight(BOLD, 8, MARGIN + 320, y, baseCol.toUpperCase(), INK_3);
        }
        if (rateCol != null) {
            drawTextRight(BOLD, 8, MARGIN + 390, y, rateCol.toUpperCase(), INK_3);
        }
        drawTextRight(BOLD, 8, MARGIN + WIDTH, y, amountCol == null ? "" : amountCol.toUpperCase(), INK_3);
        y -= 6;
        rule(2f, INK);
        return this;
    }

    /** Faint, full-width separator (light grey 1px line). */
    public PdfDocumentWriter separator() {
        ensure(8);
        rule(1f, LINE);
        return this;
    }

    /** Strong, full-width separator (ink 2px line). */
    public PdfDocumentWriter strongSeparator() {
        ensure(10);
        rule(2f, INK);
        return this;
    }

    public PdfDocumentWriter paragraph(String textBlock) {
        for (String line : wrap(REGULAR, 9, textBlock, WIDTH)) {
            text(line);
        }
        return this;
    }

    /** An italic, grey, justified clause — matches the certification footer of the preview. */
    public PdfDocumentWriter italicClause(String textBlock) {
        if (textBlock == null || textBlock.isBlank()) return this;
        for (String line : wrap(ITALIC, 8, textBlock, WIDTH)) {
            ensure(12);
            drawText(ITALIC, 8, MARGIN, y, line, INK_3);
            y -= 11;
        }
        return this;
    }

    public PdfDocumentWriter spacer(float h) {
        ensure(h);
        y -= h;
        return this;
    }

    /** A left-aligned line with explicit weight and size (e.g. a letterhead company name). */
    public PdfDocumentWriter leftText(String text, boolean bold, float size) {
        ensure(size + 5);
        drawText(bold ? BOLD : REGULAR, size, MARGIN, y, text == null ? "" : text, INK);
        y -= size + 5;
        return this;
    }

    /** A right-aligned line (regular, size 9) flush to the right margin. */
    public PdfDocumentWriter rightText(String text) {
        return rightText(text, false, 9);
    }

    /** A right-aligned line with explicit weight and size. */
    public PdfDocumentWriter rightText(String text, boolean bold, float size) {
        ensure(size + 5);
        drawTextRight(bold ? BOLD : REGULAR, size, MARGIN + WIDTH, y, text == null ? "" : text, INK);
        y -= size + 5;
        return this;
    }

    /** Centered, bold document title with a 2px ink rule the exact width of the text. */
    public PdfDocumentWriter documentTitle(String text) {
        ensure(34);
        float size = 15f;
        float width = stringWidth(BOLD, size, text);
        float x = MARGIN + (WIDTH - width) / 2;
        drawText(BOLD, size, x, y, text, INK);
        float underlineY = y - 4;
        try {
            cs.setStrokingColor(INK);
            cs.setLineWidth(2f);
            cs.moveTo(x, underlineY);
            cs.lineTo(x + width, underlineY);
            cs.stroke();
            cs.setLineWidth(1f);
            cs.setStrokingColor(Color.BLACK);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        y -= 30;
        return this;
    }

    /** A full-width horizontal rule of the given thickness (uses INK). */
    public PdfDocumentWriter rule(float thickness) {
        return rule(thickness, INK);
    }

    public PdfDocumentWriter rule(float thickness, Color color) {
        ensure(thickness + 8);
        try {
            cs.setStrokingColor(color);
            cs.setLineWidth(thickness);
            cs.moveTo(MARGIN, y);
            cs.lineTo(MARGIN + WIDTH, y);
            cs.stroke();
            cs.setLineWidth(1f);
            cs.setStrokingColor(Color.BLACK);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        y -= 8;
        return this;
    }

    /** A wrapped paragraph whose non-final lines are justified to the full content width. */
    public PdfDocumentWriter paragraphJustified(String textBlock) {
        List<String> lines = wrap(REGULAR, 9, textBlock, WIDTH);
        for (int i = 0; i < lines.size(); i++) {
            drawJustified(lines.get(i), i == lines.size() - 1);
        }
        return this;
    }

    private void drawJustified(String line, boolean last) {
        ensure(14);
        if (last || !line.contains(" ")) {
            drawText(REGULAR, 9, MARGIN, y, line, INK_2);
        } else {
            String[] words = line.split(" ");
            float wordsWidth = 0f;
            for (String word : words) {
                wordsWidth += stringWidth(REGULAR, 9, word);
            }
            float gap = (WIDTH - wordsWidth) / (words.length - 1);
            float x = MARGIN;
            for (String word : words) {
                drawText(REGULAR, 9, x, y, word, INK_2);
                x += stringWidth(REGULAR, 9, word) + gap;
            }
        }
        y -= 14;
    }

    /** Draws a full-width rectangle from the current y down by {@code height}. y is not advanced. */
    private void fillBand(Color color, float height) {
        try {
            cs.setNonStrokingColor(color);
            cs.addRect(MARGIN, y - height, WIDTH, height);
            cs.fill();
            cs.setNonStrokingColor(Color.BLACK);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] build() {
        closeStream();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.save(out);
            document.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // --- low level ---

    private void drawText(PDFont font, float size, float x, float yPos, String text, Color color) {
        String safe = sanitize(text);
        try {
            cs.beginText();
            cs.setFont(font, size);
            cs.setNonStrokingColor(color);
            cs.newLineAtOffset(x, yPos);
            cs.showText(safe);
            cs.endText();
            cs.setNonStrokingColor(Color.BLACK);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void drawTextRight(PDFont font, float size, float rightX, float yPos, String text, Color color) {
        float w = stringWidth(font, size, text);
        drawText(font, size, rightX - w, yPos, text, color);
    }

    private static float stringWidth(PDFont font, float size, String text) {
        try {
            return font.getStringWidth(sanitize(text)) / 1000 * size;
        } catch (IOException e) {
            return 0f;
        }
    }

    private List<String> wrap(PDFont font, float size, String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (stringWidth(font, size, candidate) > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    /** Replaces characters outside the WinAnsi range so Standard-14 fonts can always render them. */
    private static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00A0' || c == '\u202F' || c == '\u2009') {
                sb.append(' ');
            } else if (c == '\u2019' || c == '\u2018') {
                sb.append('\'');
            } else if (c == '\u201C' || c == '\u201D') {
                sb.append('"');
            } else if (c == '\u2013' || c == '\u2014') {
                sb.append('-');
            } else {
                sb.append(c <= 0xFF ? c : '?');
            }
        }
        return sb.toString();
    }

    private void closeStream() {
        if (cs != null) {
            try {
                cs.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            cs = null;
        }
    }
}
