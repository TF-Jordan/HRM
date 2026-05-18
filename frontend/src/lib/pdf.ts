"use client";

import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

export type PdfColumn<T> = {
  header: string;
  value: (row: T) => string | number | null | undefined;
  width?: number;
};

export type PdfMeta = {
  title: string;
  subtitle?: string;
};

const ORANGE: [number, number, number] = [242, 107, 15];
const CREAM: [number, number, number] = [248, 245, 239];
const INK: [number, number, number] = [33, 33, 33];

export function exportPdf<T>(
  filename: string,
  rows: T[],
  columns: PdfColumn<T>[],
  meta: PdfMeta,
): void {
  const doc = new jsPDF({ orientation: "landscape", unit: "pt", format: "a4" });
  const pageWidth = doc.internal.pageSize.getWidth();

  doc.setFillColor(...ORANGE);
  doc.rect(0, 0, pageWidth, 50, "F");
  doc.setTextColor(255, 255, 255);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(16);
  doc.text(meta.title, 40, 30);
  if (meta.subtitle) {
    doc.setFontSize(10);
    doc.setFont("helvetica", "normal");
    doc.text(meta.subtitle, 40, 44);
  }
  doc.setTextColor(...INK);
  doc.setFontSize(9);
  doc.text(`${new Date().toISOString().slice(0, 10)} · ${rows.length} lignes`, pageWidth - 40, 30, {
    align: "right",
  });

  autoTable(doc, {
    startY: 70,
    head: [columns.map((c) => c.header)],
    body: rows.map((row) =>
      columns.map((c) => {
        const v = c.value(row);
        return v == null ? "" : String(v);
      }),
    ),
    headStyles: {
      fillColor: CREAM,
      textColor: INK,
      fontStyle: "bold",
      fontSize: 9,
    },
    bodyStyles: {
      textColor: INK,
      fontSize: 9,
    },
    alternateRowStyles: {
      fillColor: [253, 251, 247],
    },
    margin: { left: 40, right: 40 },
    columnStyles: columns.reduce<Record<number, { cellWidth: number }>>((acc, col, i) => {
      if (col.width) acc[i] = { cellWidth: col.width };
      return acc;
    }, {}),
  });

  const finalName = filename.endsWith(".pdf") ? filename : `${filename}.pdf`;
  doc.save(finalName);
}
