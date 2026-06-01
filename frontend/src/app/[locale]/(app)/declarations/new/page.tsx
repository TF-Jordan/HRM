import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewDeclarationForm } from "@/components/declarations/new-declaration-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewDeclarationPage({
  params,
}: PageProps<"/[locale]/declarations/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:declaration:create")) redirect("/declarations");
  return <NewDeclarationForm />;
}
