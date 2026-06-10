import "server-only";

import { serverEnv } from "@/env";
import { logger } from "@/server/logger";

export type MailAttachment = {
  filename: string;
  /** Raw bytes (UTF-8 text or binary). Use Buffer for PDFs. */
  content: Buffer;
  /** Optional MIME type — defaults to application/octet-stream when omitted. */
  contentType?: string;
};

export type MailMessage = {
  to: string;
  subject: string;
  html: string;
  text: string;
  replyTo?: string;
  attachments?: MailAttachment[];
};

export type MailResult =
  | { ok: true; provider: "smtp" | "resend" | "none"; id?: string }
  | { ok: false; provider: "smtp" | "resend" | "none"; error: string };

export async function sendMail(message: MailMessage): Promise<MailResult> {
  const env = serverEnv;
  if (!env) {
    return { ok: false, provider: "none", error: "Server env not initialised" };
  }
  const provider = env.EMAIL_PROVIDER;
  const from = env.EMAIL_FROM;
  const replyTo = message.replyTo ?? env.EMAIL_REPLY_TO ?? undefined;

  try {
    if (provider === "none") {
      const attachmentList = (message.attachments ?? [])
        .map((a) => `${a.filename} (${a.content.byteLength} B)`)
        .join(", ");
      logger.warn(
        { to: message.to, subject: message.subject, from, attachments: attachmentList || undefined },
        "mailer.dispatch_skipped (EMAIL_PROVIDER=none)",
      );
      console.log(
        `\n[MAILER · NONE] from=${from} to=${message.to}\nsubject=${message.subject}\n${attachmentList ? `attachments=${attachmentList}\n` : ""}---\n${message.text}\n---\n`,
      );
      return { ok: true, provider: "none" };
    }

    if (provider === "smtp") {
      if (!env.SMTP_HOST || !env.SMTP_PORT) {
        return { ok: false, provider: "smtp", error: "SMTP_HOST or SMTP_PORT not set" };
      }
      const nodemailer = await import("nodemailer");
      const transporter = nodemailer.createTransport({
        host: env.SMTP_HOST,
        port: env.SMTP_PORT,
        secure: env.SMTP_SECURE,
        auth:
          env.SMTP_USER && env.SMTP_PASS
            ? { user: env.SMTP_USER, pass: env.SMTP_PASS }
            : undefined,
      });
      const info = await transporter.sendMail({
        from,
        to: message.to,
        subject: message.subject,
        text: message.text,
        html: message.html,
        replyTo,
        attachments: message.attachments?.map((a) => ({
          filename: a.filename,
          content: a.content,
          contentType: a.contentType,
        })),
      });
      logger.info({ to: message.to, messageId: info.messageId }, "mailer.dispatch_ok");
      return { ok: true, provider: "smtp", id: info.messageId };
    }

    if (provider === "resend") {
      if (!env.RESEND_API_KEY) {
        return { ok: false, provider: "resend", error: "RESEND_API_KEY not set" };
      }
      const { Resend } = await import("resend");
      const resend = new Resend(env.RESEND_API_KEY);
      const result = await resend.emails.send({
        from,
        to: message.to,
        subject: message.subject,
        text: message.text,
        html: message.html,
        replyTo,
        attachments: message.attachments?.map((a) => ({
          filename: a.filename,
          content: a.content,
          contentType: a.contentType,
        })),
      });
      if (result.error) {
        return { ok: false, provider: "resend", error: result.error.message };
      }
      logger.info({ to: message.to, id: result.data?.id }, "mailer.dispatch_ok");
      return { ok: true, provider: "resend", id: result.data?.id };
    }

    return { ok: false, provider: "none", error: `Unknown EMAIL_PROVIDER ${provider}` };
  } catch (cause) {
    logger.error({ cause: String(cause), to: message.to }, "mailer.dispatch_failed");
    return { ok: false, provider, error: cause instanceof Error ? cause.message : String(cause) };
  }
}
