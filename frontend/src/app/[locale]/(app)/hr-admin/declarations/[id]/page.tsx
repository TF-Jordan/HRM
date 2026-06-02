import { setRequestLocale } from "next-intl/server";

import { DeclarationDetail } from "@/components/declarations/declaration-detail";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/declarations/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <DeclarationDetail declarationId={id} />;
}
