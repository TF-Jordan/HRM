import { setRequestLocale } from "next-intl/server";

import { NewLeaveForm } from "@/components/leaves/new-leave-form";

export default async function NewLeavePage({ params }: PageProps<"/[locale]/leaves/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewLeaveForm />;
}
