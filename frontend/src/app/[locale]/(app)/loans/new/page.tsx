import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewLoanForm } from "@/components/loans/new-loan-form";
import { readSession } from "@/server/session";

export default async function NewLoanPage({ params }: PageProps<"/[locale]/loans/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  const canCreate = session.user.permissions.some((p) => p.startsWith("hrm:loan:create"));
  if (!canCreate) redirect("/loans");
  return <NewLoanForm />;
}
