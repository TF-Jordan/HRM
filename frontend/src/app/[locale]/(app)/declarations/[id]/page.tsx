import { setRequestLocale } from "next-intl/server";

import { DeclarationDetail } from "@/components/declarations/declaration-detail";

export default async function DeclarationDetailPage({
  params,
}: PageProps<"/[locale]/declarations/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <DeclarationDetail declarationId={id} />;
}
