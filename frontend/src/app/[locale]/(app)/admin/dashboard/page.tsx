import { setRequestLocale } from "next-intl/server";

import { AdminLanding } from "@/components/admin/admin-landing";

export default async function Page({ params }: PageProps<"/[locale]/admin/dashboard">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <AdminLanding />;
}
