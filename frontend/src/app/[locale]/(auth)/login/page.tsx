import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { LoginForm } from "@/components/auth/login-form";
import { readSession } from "@/server/session";

export default async function LoginPage({
  params,
  searchParams,
}: PageProps<"/[locale]/login">) {
  const { locale } = await params;
  setRequestLocale(locale);

  // If already authenticated, send the user to the dashboard.
  const session = await readSession();
  if (session) {
    redirect("/dashboard");
  }

  const reasonRaw = (await searchParams).reason;
  const reason = Array.isArray(reasonRaw) ? reasonRaw[0] : reasonRaw;

  return <LoginForm reason={reason} />;
}
