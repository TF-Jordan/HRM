import { setRequestLocale } from "next-intl/server";

import { UserCreateForm } from "@/components/admin/user-create-form";

export default async function AdminUserNewPage({
  params,
}: PageProps<"/[locale]/admin/users/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <UserCreateForm />;
}
