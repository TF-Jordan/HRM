import { setRequestLocale } from "next-intl/server";

import { NewDeclarationForm } from "@/components/declarations/new-declaration-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/declarations/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewDeclarationForm />;
}
