import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { SelectContextForm } from "@/components/auth/select-context-form";
import { readSession } from "@/server/session";

export default async function SelectContextPage({ params }: PageProps<"/[locale]/select-context">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (session) {
    redirect(session.forcePasswordChange ? "/change-password" : "/dashboard");
  }

  return <SelectContextForm />;
}
