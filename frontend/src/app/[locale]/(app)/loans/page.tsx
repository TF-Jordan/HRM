import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { LoansOverview } from "@/components/loans/loans-overview";
import { readSession } from "@/server/session";

export default async function LoansIndexPage({ params }: PageProps<"/[locale]/loans">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  // Approver / HR admin → org-wide overview. Plain employee → self-service.
  const canApprove = session.user.permissions.some((p) =>
    p.startsWith("hrm:loan:approve"),
  );
  if (!canApprove) redirect("/loans/mine");
  return <LoansOverview />;
}
