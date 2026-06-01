import { setRequestLocale } from "next-intl/server";

import { UsersList } from "@/components/admin/users-list";

export default async function AdminUsersPage({ params }: PageProps<"/[locale]/admin/users">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <UsersList />;
}
