import "server-only";

import { clientEnv } from "@/env";

export type WelcomeMailInput = {
  firstName: string;
  lastName: string;
  email: string;
  matricule: string;
  temporaryPassword: string;
  organizationName: string;
  locale?: "fr" | "en";
};

export function welcomeMail(input: WelcomeMailInput): { subject: string; html: string; text: string } {
  const locale = input.locale ?? "fr";
  const appUrl = clientEnv.NEXT_PUBLIC_APP_URL.replace(/\/$/, "");
  const loginUrl = `${appUrl}/${locale}/login`;
  const fullName = `${input.firstName} ${input.lastName}`.trim();

  if (locale === "en") {
    return {
      subject: `Welcome to ${input.organizationName} · your HR Core account`,
      text: enText({ ...input, fullName, loginUrl }),
      html: enHtml({ ...input, fullName, loginUrl }),
    };
  }
  return {
    subject: `Bienvenue chez ${input.organizationName} · votre compte HR Core`,
    text: frText({ ...input, fullName, loginUrl }),
    html: frHtml({ ...input, fullName, loginUrl }),
  };
}

type Ctx = WelcomeMailInput & { fullName: string; loginUrl: string };

function frText(c: Ctx): string {
  return [
    `Bonjour ${c.firstName},`,
    ``,
    `Votre compte HR Core a été créé pour ${c.organizationName}.`,
    `Matricule : ${c.matricule}`,
    ``,
    `Identifiants de connexion :`,
    `  Email   : ${c.email}`,
    `  Mot de passe temporaire : ${c.temporaryPassword}`,
    ``,
    `Lien de connexion : ${c.loginUrl}`,
    ``,
    `Pour des raisons de sécurité, ce mot de passe est temporaire.`,
    `À votre première connexion, l'application vous demandera de le remplacer`,
    `par un mot de passe personnel.`,
    ``,
    `Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.`,
    ``,
    `— L'équipe HR Core`,
  ].join("\n");
}

function enText(c: Ctx): string {
  return [
    `Hi ${c.firstName},`,
    ``,
    `Your HR Core account has been created for ${c.organizationName}.`,
    `Employee ID: ${c.matricule}`,
    ``,
    `Sign-in credentials:`,
    `  Email    : ${c.email}`,
    `  Temp pwd : ${c.temporaryPassword}`,
    ``,
    `Sign-in link: ${c.loginUrl}`,
    ``,
    `For security reasons, this password is temporary.`,
    `On your first sign-in, you will be prompted to replace it with`,
    `a personal password.`,
    ``,
    `If you did not request this, please ignore this message.`,
    ``,
    `— HR Core team`,
  ].join("\n");
}

function frHtml(c: Ctx): string {
  return shell(
    "Bienvenue sur HR Core",
    `
    <p style="margin:0 0 18px 0;">Bonjour <b>${escape(c.fullName)}</b>,</p>
    <p style="margin:0 0 18px 0;">
      Votre compte <b>HR Core</b> a été créé pour <b>${escape(c.organizationName)}</b>.
      <br/>Matricule&nbsp;: <b style="font-family:monospace">${escape(c.matricule)}</b>
    </p>
    <p style="margin:0 0 8px 0;">Vos identifiants de connexion&nbsp;:</p>
    <table cellpadding="0" cellspacing="0" style="margin:0 0 22px 0;font:13px/1.4 ui-sans-serif,system-ui;">
      <tr><td style="padding:4px 12px 4px 0;color:#6B7280">Email</td>
          <td style="padding:4px 0;font-family:monospace"><b>${escape(c.email)}</b></td></tr>
      <tr><td style="padding:4px 12px 4px 0;color:#6B7280">Mot de passe temporaire</td>
          <td style="padding:4px 0;font-family:monospace"><b>${escape(c.temporaryPassword)}</b></td></tr>
    </table>
    <p style="margin:0 0 22px 0;">
      <a href="${escape(c.loginUrl)}"
         style="display:inline-block;padding:11px 18px;background:#F97316;color:#fff;
                text-decoration:none;border-radius:10px;font-weight:700;">
        Se connecter à HR Core
      </a>
    </p>
    <p style="margin:0 0 18px 0;color:#6B7280;font-size:12.5px;">
      Pour des raisons de sécurité, ce mot de passe est temporaire. À votre première connexion,
      l'application vous demandera de le remplacer par un mot de passe personnel.
    </p>
    <p style="margin:0;color:#9CA3AF;font-size:11.5px;">
      Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.
    </p>
    `,
  );
}

function enHtml(c: Ctx): string {
  return shell(
    "Welcome to HR Core",
    `
    <p style="margin:0 0 18px 0;">Hi <b>${escape(c.fullName)}</b>,</p>
    <p style="margin:0 0 18px 0;">
      Your <b>HR Core</b> account has been created for <b>${escape(c.organizationName)}</b>.
      <br/>Employee ID: <b style="font-family:monospace">${escape(c.matricule)}</b>
    </p>
    <p style="margin:0 0 8px 0;">Your sign-in credentials:</p>
    <table cellpadding="0" cellspacing="0" style="margin:0 0 22px 0;font:13px/1.4 ui-sans-serif,system-ui;">
      <tr><td style="padding:4px 12px 4px 0;color:#6B7280">Email</td>
          <td style="padding:4px 0;font-family:monospace"><b>${escape(c.email)}</b></td></tr>
      <tr><td style="padding:4px 12px 4px 0;color:#6B7280">Temp. password</td>
          <td style="padding:4px 0;font-family:monospace"><b>${escape(c.temporaryPassword)}</b></td></tr>
    </table>
    <p style="margin:0 0 22px 0;">
      <a href="${escape(c.loginUrl)}"
         style="display:inline-block;padding:11px 18px;background:#F97316;color:#fff;
                text-decoration:none;border-radius:10px;font-weight:700;">
        Sign in to HR Core
      </a>
    </p>
    <p style="margin:0 0 18px 0;color:#6B7280;font-size:12.5px;">
      For security reasons, this password is temporary. On your first sign-in, you will
      be prompted to replace it with a personal password.
    </p>
    <p style="margin:0;color:#9CA3AF;font-size:11.5px;">
      If you did not request this, please ignore this message.
    </p>
    `,
  );
}

function shell(title: string, body: string): string {
  return `<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${escape(title)}</title>
</head>
<body style="margin:0;padding:32px 16px;background:#F8FAFC;font:14px/1.5 ui-sans-serif,system-ui,-apple-system,Segoe UI,Roboto,sans-serif;color:#1F2937;">
  <table cellpadding="0" cellspacing="0" align="center" style="max-width:560px;width:100%;background:#fff;border:1px solid #E5E7EB;border-radius:18px;box-shadow:0 4px 24px rgba(15,23,42,0.06);">
    <tr><td style="padding:26px 30px 0;">
      <div style="display:inline-flex;align-items:center;gap:10px;">
        <span style="display:inline-block;width:32px;height:32px;border-radius:8px;background:linear-gradient(135deg,#F97316,#EA580C);color:#fff;text-align:center;line-height:32px;font-weight:800;">H</span>
        <span style="font-weight:800;font-size:16px;letter-spacing:-0.01em;">HR Core</span>
      </div>
    </td></tr>
    <tr><td style="padding:22px 30px 28px;">
      ${body}
    </td></tr>
  </table>
</body>
</html>`;
}

function escape(s: string): string {
  return s
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/* ============================== Payslip ============================== */

export type PayslipMailInput = {
  employeeName: string;
  matricule: string;
  periode: string; // "YYYY-MM"
  netToPay: string; // already formatted, e.g. "650 000"
  currency: string; // e.g. "XAF"
  organizationName: string;
  locale?: "fr" | "en";
};

/**
 * Cover email for a single employee's payslip PDF — the actual document travels as an
 * attachment so it can be archived as-is. The body intentionally restates the period and
 * net amount so the recipient can verify the attachment matches before opening it.
 */
export function payslipMail(input: PayslipMailInput): { subject: string; html: string; text: string } {
  const locale = input.locale ?? "fr";
  const periodLabel = formatPeriod(input.periode, locale);
  if (locale === "en") {
    return {
      subject: `Your payslip · ${periodLabel}`,
      text: payslipEnText({ ...input, periodLabel }),
      html: payslipEnHtml({ ...input, periodLabel }),
    };
  }
  return {
    subject: `Votre bulletin de paie · ${periodLabel}`,
    text: payslipFrText({ ...input, periodLabel }),
    html: payslipFrHtml({ ...input, periodLabel }),
  };
}

type PayslipCtx = PayslipMailInput & { periodLabel: string };

function payslipFrText(c: PayslipCtx): string {
  return [
    `Bonjour ${c.employeeName},`,
    ``,
    `Veuillez trouver ci-joint votre bulletin de paie pour la période ${c.periodLabel}.`,
    ``,
    `  Matricule    : ${c.matricule}`,
    `  Période      : ${c.periodLabel}`,
    `  Net à payer  : ${c.netToPay} ${c.currency}`,
    ``,
    `Le document PDF joint est signé électroniquement par ${c.organizationName}.`,
    `Pour toute question, contactez votre service Ressources humaines.`,
    ``,
    `— HR Core`,
  ].join("\n");
}

function payslipEnText(c: PayslipCtx): string {
  return [
    `Hello ${c.employeeName},`,
    ``,
    `Please find attached your payslip for ${c.periodLabel}.`,
    ``,
    `  Employee #   : ${c.matricule}`,
    `  Period       : ${c.periodLabel}`,
    `  Net to pay   : ${c.netToPay} ${c.currency}`,
    ``,
    `The attached PDF is digitally signed by ${c.organizationName}.`,
    `For any question, contact your HR department.`,
    ``,
    `— HR Core`,
  ].join("\n");
}

function payslipFrHtml(c: PayslipCtx): string {
  return shell(
    "Votre bulletin de paie",
    `
    <p style="margin:0 0 18px 0;">Bonjour <b>${escape(c.employeeName)}</b>,</p>
    <p style="margin:0 0 14px 0;">
      Veuillez trouver ci-joint votre bulletin de paie pour la période
      <b>${escape(c.periodLabel)}</b>.
    </p>
    ${payslipSummaryTable(c, "fr")}
    <p style="margin:18px 0 8px 0;color:#6B7280;font-size:12.5px;">
      Le document PDF joint est signé électroniquement par
      <b>${escape(c.organizationName)}</b>.
    </p>
    <p style="margin:0;color:#9CA3AF;font-size:11.5px;">
      Pour toute question, contactez votre service Ressources humaines.
    </p>
    `,
  );
}

function payslipEnHtml(c: PayslipCtx): string {
  return shell(
    "Your payslip",
    `
    <p style="margin:0 0 18px 0;">Hello <b>${escape(c.employeeName)}</b>,</p>
    <p style="margin:0 0 14px 0;">
      Please find attached your payslip for <b>${escape(c.periodLabel)}</b>.
    </p>
    ${payslipSummaryTable(c, "en")}
    <p style="margin:18px 0 8px 0;color:#6B7280;font-size:12.5px;">
      The attached PDF is digitally signed by
      <b>${escape(c.organizationName)}</b>.
    </p>
    <p style="margin:0;color:#9CA3AF;font-size:11.5px;">
      For any question, contact your HR department.
    </p>
    `,
  );
}

function payslipSummaryTable(c: PayslipCtx, locale: "fr" | "en"): string {
  const L = locale === "fr"
    ? { matricule: "Matricule", period: "Période", net: "Net à payer" }
    : { matricule: "Employee #", period: "Period", net: "Net to pay" };
  return `
    <table cellpadding="0" cellspacing="0" style="margin:8px 0 0 0;border-collapse:collapse;font:13px/1.4 ui-sans-serif,system-ui;">
      <tr><td style="padding:6px 14px 6px 0;color:#6B7280;">${L.matricule}</td>
          <td style="padding:6px 0;font-family:monospace"><b>${escape(c.matricule)}</b></td></tr>
      <tr><td style="padding:6px 14px 6px 0;color:#6B7280;">${L.period}</td>
          <td style="padding:6px 0;"><b>${escape(c.periodLabel)}</b></td></tr>
      <tr><td style="padding:10px 14px 6px 0;color:#6B7280;border-top:1px solid #E5E7EB;">${L.net}</td>
          <td style="padding:10px 0 6px 0;font-family:monospace;border-top:1px solid #E5E7EB;">
            <b style="font-size:15px;color:#1A150E;">${escape(c.netToPay)} ${escape(c.currency)}</b>
          </td></tr>
    </table>`;
}

function formatPeriod(periode: string, locale: "fr" | "en"): string {
  const parts = periode.split("-");
  const y = Number(parts[0]);
  const m = Number(parts[1]);
  if (!y || !m || m < 1 || m > 12) return periode;
  const date = new Date(y, m - 1, 1);
  return date.toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", {
    month: "long",
    year: "numeric",
  });
}
