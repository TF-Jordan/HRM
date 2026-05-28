import { setRequestLocale } from "next-intl/server";

import { OrganizationView } from "@/components/admin/organization-view";

export default async function AdminOrganizationPage({
  params,
}: PageProps<"/[locale]/admin/organization">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <OrganizationView />;
}
