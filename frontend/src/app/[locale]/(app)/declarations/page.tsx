import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { DeclarationsQueue } from "@/components/declarations/declarations-queue";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function DeclarationsIndexPage({
  params,
}: PageProps<"/[locale]/declarations">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:declaration:read")) redirect("/dashboard");
  return <DeclarationsQueue />;
}
