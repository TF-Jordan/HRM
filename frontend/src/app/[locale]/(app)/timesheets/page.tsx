import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { TimesheetsAdmin } from "@/components/timesheets/timesheets-admin";
import { readSession } from "@/server/session";

export default async function TimesheetsPage({ params }: PageProps<"/[locale]/timesheets">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  // Managers / payroll (timesheet:validate) get the org consolidation view.
  // Everyone else — including plain employees who only hold timesheet:read on
  // their own data — goes to the self-service entries.
  const canValidate = session.user.permissions.includes("hrm:timesheet:validate");
  if (!canValidate) redirect("/timesheets/my");
  return <TimesheetsAdmin />;
}
