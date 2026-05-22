"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Check, Copy, Eye, EyeOff, AlertTriangle, Mail, KeyRound } from "lucide-react";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

export type CredentialsModalProps = {
  open: boolean;
  onClose: () => void;
  matricule: string;
  email: string;
  temporaryPassword: string;
  emailSent: boolean;
  roleAssigned: string | null;
  membershipCreated: boolean;
};

export function CredentialsModal({
  open,
  onClose,
  matricule,
  email,
  temporaryPassword,
  emailSent,
  roleAssigned,
  membershipCreated,
}: CredentialsModalProps) {
  const t = useTranslations("employees.credentialsModal");
  const tCommon = useTranslations("common");
  const [pwdVisible, setPwdVisible] = React.useState(false);
  const [copied, setCopied] = React.useState<"matricule" | "password" | null>(null);

  const copy = (value: string, kind: "matricule" | "password") => {
    void navigator.clipboard.writeText(value).then(() => {
      setCopied(kind);
      toast.success(tCommon("copied"));
      setTimeout(() => setCopied(null), 2000);
    });
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <KeyRound className="size-5 text-brand-600" />
            {t("title")}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div className="rounded-xl border border-status-amber-500 bg-status-amber-50 px-4 py-3 dark:bg-dark-3">
            <div className="flex items-start gap-2">
              <AlertTriangle className="size-4 shrink-0 text-status-amber-600" />
              <p className="text-[12.5px] text-status-amber-600">{t("warning")}</p>
            </div>
          </div>

          <div className="space-y-3">
            <CredField
              label={t("matricule")}
              value={matricule}
              monospace
              size="lg"
              onCopy={() => copy(matricule, "matricule")}
              copied={copied === "matricule"}
            />
            <CredField
              label={t("password")}
              value={temporaryPassword}
              monospace
              size="lg"
              masked={!pwdVisible}
              onToggleMask={() => setPwdVisible((v) => !v)}
              onCopy={() => copy(temporaryPassword, "password")}
              copied={copied === "password"}
            />
          </div>

          <div className="space-y-1.5 rounded-xl bg-cream-soft/40 px-4 py-3 dark:bg-dark-3">
            <div className="flex items-center gap-2 text-[12.5px]">
              <Mail className="size-3.5 text-ink-3" />
              <span className="text-ink-2">{t("sentTo")} </span>
              <span className="font-medium text-ink">{email}</span>
              {emailSent ? (
                <span className="ml-1 inline-flex items-center gap-1 text-status-green-600">
                  <Check className="size-3.5" />
                  {t("emailSent")}
                </span>
              ) : (
                <span className="ml-1 text-status-amber-600">{t("emailPending")}</span>
              )}
            </div>
            {roleAssigned && (
              <div className="text-[12.5px] text-ink-3">
                {t("roleAssigned")}{" "}
                <span className="font-mono font-semibold text-brand-700">{roleAssigned}</span>
              </div>
            )}
            {!membershipCreated && (
              <div className="flex items-center gap-2 text-[12.5px] text-status-amber-600">
                <AlertTriangle className="size-3.5" />
                {t("membershipMissing")}
              </div>
            )}
          </div>
        </div>

        <DialogFooter>
          <Button onClick={onClose}>{t("acknowledge")}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function CredField({
  label,
  value,
  monospace,
  size,
  masked,
  onToggleMask,
  onCopy,
  copied,
}: {
  label: string;
  value: string;
  monospace?: boolean;
  size?: "lg" | "md";
  masked?: boolean;
  onToggleMask?: () => void;
  onCopy: () => void;
  copied: boolean;
}) {
  const displayed = masked ? "•".repeat(Math.min(value.length, 16)) : value;
  return (
    <div>
      <div className="mb-1 text-[11px] font-semibold uppercase tracking-wide text-ink-4">
        {label}
      </div>
      <div className="flex items-center gap-2 rounded-xl border border-line bg-white px-3 py-2 dark:border-dark-line dark:bg-dark-2">
        <span
          className={
            (monospace ? "font-mono " : "") +
            (size === "lg" ? "text-[18px] " : "text-[14px] ") +
            "grow font-semibold tracking-wide text-ink"
          }
        >
          {displayed}
        </span>
        {onToggleMask && (
          <button
            type="button"
            onClick={onToggleMask}
            className="grid size-8 place-items-center rounded-lg text-ink-3 hover:bg-cream-soft hover:text-ink dark:hover:bg-dark-3"
            aria-label="Toggle visibility"
          >
            {masked ? <Eye className="size-4" /> : <EyeOff className="size-4" />}
          </button>
        )}
        <button
          type="button"
          onClick={onCopy}
          className="grid size-8 place-items-center rounded-lg text-ink-3 hover:bg-cream-soft hover:text-ink dark:hover:bg-dark-3"
          aria-label="Copy"
        >
          {copied ? <Check className="size-4 text-status-green-600" /> : <Copy className="size-4" />}
        </button>
      </div>
    </div>
  );
}
