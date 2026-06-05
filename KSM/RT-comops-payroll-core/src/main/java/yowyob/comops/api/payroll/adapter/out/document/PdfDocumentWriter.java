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
 * titles, key/value lines, table rows, separators and wrapped paragraphs, plus automatic page
 * breaks. Pure (no Spring); produces the document bytes via {@link #build()}.
 *
 * Text is rendered with the Standard-14 Helvetica fonts (WinAnsi), which cover French accents;
 * any character outside that encoding is sanitised so rendering never fails.
 */
public final class PdfDocumentWriter {

    private static final PDFont REGULAR = PDType1Font.HELVETICA;
    private static final PDFont BOLD = PDType1Font.HELVETICA_BOLD;
    private static final float MARGIN = 50f;
    private static final float TOP = PDRectangle.A4.getHeight() - MARGIN;
    private static final float BOTTOM = MARGIN;
    private static final float WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;

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
        drawText(BOLD, 16, MARGIN + (WIDTH - width) / 2, y, text);
        y -= 30;
        return this;
    }

    public PdfDocumentWriter heading(String text) {
        ensure(20);
        y -= 6;
        drawText(BOLD, 11, MARGIN, y, text);
        y -= 16;
        return this;
    }

    public PdfDocumentWriter text(String text) {
        ensure(14);
        drawText(REGULAR, 9, MARGIN, y, text);
        y -= 13;
        return this;
    }

    /** A label (bold) + value on one line. */
    public PdfDocumentWriter kv(String label, String value) {
        ensure(14);
        drawText(BOLD, 9, MARGIN, y, label);
        drawText(REGULAR, 9, MARGIN + 150, y, value == null ? "" : value);
        y -= 14;
        return this;
    }

    /** A table row: each cell at its x; the last cell (amount) is right-aligned to the page edge. */
    public PdfDocumentWriter row(String label, String base, String rate, String amount, boolean bold) {
        ensure(15);
        PDFont font = bold ? BOLD : REGULAR;
        drawText(font, 9, MARGIN, y, label);
        if (base != null) {
            drawTextRight(REGULAR, 9, MARGIN + 320, y, base);
        }
        if (rate != null) {
            drawTextRight(REGULAR, 9, MARGIN + 390, y, rate);
        }
        drawTextRight(font, 9, MARGIN + WIDTH, y, amount == null ? "" : amount);
        y -= 14;
        return this;
    }

    public PdfDocumentWriter separator() {
        ensure(8);
        try {
            cs.setStrokingColor(Color.LIGHT_GRAY);
            cs.moveTo(MARGIN, y);
            cs.lineTo(MARGIN + WIDTH, y);
            cs.stroke();
            cs.setStrokingColor(Color.BLACK);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        y -= 8;
        return this;
    }

    public PdfDocumentWriter paragraph(String textBlock) {
        for (String line : wrap(REGULAR, 9, textBlock, WIDTH)) {
            text(line);
        }
        return this;
    }

    public PdfDocumentWriter spacer(float h) {
        ensure(h);
        y -= h;
        return this;
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

    private void drawText(PDFont font, float size, float x, float yPos, String text) {
        String safe = sanitize(text);
        try {
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, yPos);
            cs.showText(safe);
            cs.endText();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void drawTextRight(PDFont font, float size, float rightX, float yPos, String text) {
        float w = stringWidth(font, size, text);
        drawText(font, size, rightX - w, yPos, text);
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
