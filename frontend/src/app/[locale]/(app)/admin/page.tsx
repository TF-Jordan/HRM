import { setRequestLocale } from "next-intl/server";

import { AdminLanding } from "@/components/admin/admin-landing";

export default async function AdminLandingPage({ params }: PageProps<"/[locale]/admin">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <AdminLanding />;
}
