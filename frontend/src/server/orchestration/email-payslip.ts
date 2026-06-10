import "server-only";

import { serverEnv } from "@/env";
import { sendMail, type MailResult } from "@/server/email/mailer";
import { payslipMail } from "@/server/email/templates";
import { callKsm } from "@/server/ksm/client";
import { logger } from "@/server/logger";
import * as profileApi from "@/server/ksm/modules/employee-profile";
import * as payrollApi from "@/server/ksm/modules/payroll";
import type { AppSession } from "@/lib/types/auth";

/**
 * Payroll cycle statuses for which payslips can be emailed. The HR admin must validate
 * the cycle first; rejecting or recalculating the cycle invalidates the signed document
 * and the button is disabled in the UI accordingly.
 */
const EMAILABLE_STATUSES = new Set<string>([
  "VALIDATED",
  "APPROVED",
  "PAYMENT_INITIATED",
  "PAID",
  "CLOSED",
]);

export function isEmailable(status: string): boolean {
  return EMAILABLE_STATUSES.has(status);
}

type DocumentResponse = {
  id: string;
  fileId: string;
  fileName: string;
  type: string;
  verificationCode: string;
};

export type PayslipEmailOutcome = {
  entryId: string;
  employeeId: string;
  email: string | null;
  ok: boolean;
  reason?: string;
};

/**
 * Send one signed payslip PDF to its employee. Both the parent run (for status + periode)
 * and the entry (for the net amount) are loaded once so the template can show a faithful
 * summary alongside the attachment.
 */
export async function emailSinglePayslip(
  runId: string,
  entryId: string,
  session: AppSession,
): Promise<{ runStatus: string; outcome: PayslipEmailOutcome }> {
  const run = await payrollApi.getPayrollRun(runId, session);
  if (!isEmailable(run.status)) {
    return {
      runStatus: run.status,
      outcome: { entryId, employeeId: "", email: null, ok: false, reason: "RUN_NOT_VALIDATED" },
    };
  }
  const entries = await payrollApi.listPayrollEntries(runId, session);
  const entry = entries.find((e) => e.id === entryId);
  if (!entry) {
    return {
      runStatus: run.status,
      outcome: { entryId, employeeId: "", email: null, ok: false, reason: "ENTRY_NOT_FOUND" },
    };
  }
  const outcome = await doSend(entry.id, entry.employeeId, run.periode, String(entry.net ?? 0), session);
  return { runStatus: run.status, outcome };
}

/**
 * Bulk send: for the given payroll run, dispatch one signed PDF per entry. Returns a
 * per-employee outcome list so the UI can show successes vs. failures (missing email,
 * file unavailable, mailer error). Hard-fails only when the run itself can't be loaded.
 */
export async function emailAllPayslipsForRun(
  runId: string,
  session: AppSession,
): Promise<{ runStatus: string; results: PayslipEmailOutcome[] }> {
  const run = await payrollApi.getPayrollRun(runId, session);
  if (!isEmailable(run.status)) {
    return { runStatus: run.status, results: [] };
  }
  const entries = await payrollApi.listPayrollEntries(runId, session);
  const results: PayslipEmailOutcome[] = [];
  // Run sequentially to keep load light on KSM and the mail provider, and to keep the
  // outcome list deterministic for the UI summary.
  for (const entry of entries) {
    const outcome = await doSend(
      entry.id,
      entry.employeeId,
      run.periode,
      String(entry.net ?? 0),
      session,
    );
    results.push(outcome);
  }
  return { runStatus: run.status, results };
}

async function doSend(
  entryId: string,
  employeeId: string,
  periode: string,
  netToPay: string,
  session: AppSession,
): Promise<PayslipEmailOutcome> {
  // 1. Resolve the recipient email + display name + matricule via the profile endpoint
  //    (which carries actorEmail joined from actor-core).
  let recipientEmail: string | null = null;
  let employeeName = "";
  let matricule = "";
  try {
    const profile = await profileApi.getEmployeeProfile(employeeId, session);
    recipientEmail = (profile.actorEmail ?? "").trim() || null;
    employeeName = (profile.actorDisplayName
      ?? `${profile.actorFirstName ?? ""} ${profile.actorLastName ?? ""}`.trim()
      ?? "").toString();
    matricule = profile.matricule ?? "";
  } catch (cause) {
    logger.warn({ employeeId, cause: String(cause) }, "payslipMail.profile_lookup_failed");
  }
  if (!recipientEmail) {
    return { entryId, employeeId, email: null, ok: false, reason: "NO_EMAIL" };
  }

  // 2. Ensure a signed PDF exists for this entry, then fetch its bytes from file-core.
  let fileBytes: Buffer;
  let fileName: string;
  try {
    const doc = await callKsm<DocumentResponse>(
      `/api/v1/payroll/documents/payslip?entryId=${entryId}`,
      { method: "POST" },
      { session },
    );
    fileName = doc.fileName || `bulletin-${periode}-${matricule || entryId.slice(0, 6)}.pdf`;
    fileBytes = await downloadFile(doc.fileId, session);
  } catch (cause) {
    logger.warn({ entryId, employeeId, cause: String(cause) }, "payslipMail.pdf_generation_failed");
    return { entryId, employeeId, email: recipientEmail, ok: false, reason: "PDF_UNAVAILABLE" };
  }

  // 3. Send the email with the PDF attached.
  const orgName = session.workspace?.organizationName ?? "HR Core";
  const message = payslipMail({
    employeeName: employeeName || recipientEmail,
    matricule: matricule || "—",
    periode,
    netToPay: formatXaf(netToPay),
    currency: "XAF",
    organizationName: orgName,
    locale: "fr",
  });
  const result: MailResult = await sendMail({
    to: recipientEmail,
    subject: message.subject,
    text: message.text,
    html: message.html,
    attachments: [
      { filename: fileName, content: fileBytes, contentType: "application/pdf" },
    ],
  });
  if (!result.ok) {
    return { entryId, employeeId, email: recipientEmail, ok: false, reason: result.error };
  }
  return { entryId, employeeId, email: recipientEmail, ok: true };
}

async function downloadFile(fileId: string, session: AppSession): Promise<Buffer> {
  if (!serverEnv) throw new Error("server env not initialised");
  const upstream = await fetch(`${serverEnv.KSM_BASE_URL}/api/files/${fileId}`, {
    headers: {
      "X-Client-Id": serverEnv.KSM_CLIENT_ID,
      "X-Api-Key": serverEnv.KSM_API_KEY,
      Authorization: `Bearer ${session.accessToken}`,
      "X-Tenant-Id": session.user.tenantId,
      ...(session.workspace?.organizationId
        ? { "X-Organization-Id": session.workspace.organizationId }
        : {}),
    },
    cache: "no-store",
  });
  if (!upstream.ok) {
    throw new Error(`file-core ${upstream.status}`);
  }
  const buf = await upstream.arrayBuffer();
  return Buffer.from(buf);
}

function formatXaf(amount: string | number): string {
  const n = typeof amount === "number" ? amount : Number(amount);
  if (!Number.isFinite(n)) return String(amount);
  return new Intl.NumberFormat("fr-FR", { maximumFractionDigits: 0 }).format(n);
}
