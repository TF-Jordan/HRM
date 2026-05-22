import { setRequestLocale } from "next-intl/server";
import { AdminUsersClient } from "./admin-users-client";

export default async function AdminUsersPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <AdminUsersClient />;
}
