import { setRequestLocale } from "next-intl/server";
import { AdminUserDetailClient } from "./admin-user-detail-client";

export default async function AdminUserDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <AdminUserDetailClient userId={id} />;
}
