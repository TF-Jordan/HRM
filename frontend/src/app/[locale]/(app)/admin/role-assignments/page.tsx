import { setRequestLocale } from "next-intl/server";

import { RoleAssignments } from "@/components/admin/role-assignments";

export default async function AdminRoleAssignmentsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <RoleAssignments />;
}
