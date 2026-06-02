import { setRequestLocale } from "next-intl/server";

import { NewLeaveForm } from "@/components/leaves/new-leave-form";

export default async function Page({ params }: PageProps<"/[locale]/employee/leaves/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewLeaveForm />;
}
