import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { LoansQueue } from "@/components/loans/loans-queue";
import { MyLoans } from "@/components/loans/my-loans";
import { readSession } from "@/server/session";

export default async function LoansPage({ params }: PageProps<"/[locale]/loans">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  // Admin / HR manager → management queue. Employee → self-service.
  const perms = (session.user.permissions ?? []).map((p) => p.split("#")[0]);
  const canManageLoans = perms.includes("hrm:loan:read") || perms.includes("hrm:loan:approve");

  if (canManageLoans) {
    return <LoansQueue />;
  }
  return <MyLoans />;
}
