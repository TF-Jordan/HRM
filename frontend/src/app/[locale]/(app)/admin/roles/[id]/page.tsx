import { setRequestLocale } from "next-intl/server";
import { AdminRoleDetailClient } from "./admin-role-detail-client";

export default async function AdminRoleDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <AdminRoleDetailClient roleId={id} />;
}
