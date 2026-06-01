import { setRequestLocale } from "next-intl/server";

import { RolesList } from "@/components/admin/roles-list";

export default async function AdminRolesPage({ params }: PageProps<"/[locale]/admin/roles">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <RolesList />;
}
