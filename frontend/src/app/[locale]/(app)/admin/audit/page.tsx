import { setRequestLocale } from "next-intl/server";

import { AuditLog } from "@/components/admin/audit-log";

export default async function AdminAuditPage({ params }: PageProps<"/[locale]/admin/audit">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <AuditLog />;
}
