"use client";

import { useTranslations } from "next-intl";
import * as React from "react";

import { ChangePasswordForm } from "@/components/auth/change-password-form";
import { useSession } from "@/components/providers/session-provider";
import { Dialog } from "@/components/ui/dialog";

/**
 * Blocks the entire app with a mandatory password-change dialog when the session
 * carries forcePasswordChange. The user cannot dismiss it — they must set a
 * personal password before accessing HR Core.
 */
export function ForcePasswordChangeGate() {
  const t = useTranslations("auth.changePassword");
  const { session, refresh } = useSession();
  const forced = session?.forcePasswordChange === true;

  React.useEffect(() => {
    if (!forced) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [forced]);

  if (!forced) return null;

  return (
    <Dialog
      open
      dismissible={false}
      onClose={() => {}}
      size="sm"
      title={t("title")}
      subtitle={t("subtitle")}
    >
      <ChangePasswordForm
        forced
        embedded
        onSuccess={async () => {
          await refresh();
        }}
      />
    </Dialog>
  );
}
