import { setRequestLocale } from "next-intl/server";
import { AdminRolesClient } from "./admin-roles-client";

export default async function AdminRolesPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <AdminRolesClient />;
}
