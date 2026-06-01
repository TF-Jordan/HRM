import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { ChangePasswordForm } from "@/components/auth/change-password-form";
import { readSession } from "@/server/session";

export default async function ChangePasswordPage({
  params,
}: PageProps<"/[locale]/change-password">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) {
    redirect("/login");
  }

  return <ChangePasswordForm forced={session.forcePasswordChange === true} />;
}
