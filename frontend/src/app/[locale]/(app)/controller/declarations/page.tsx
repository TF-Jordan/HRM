import { setRequestLocale } from "next-intl/server";

import { DeclarationsQueue } from "@/components/declarations/declarations-queue";

export default async function Page({ params }: PageProps<"/[locale]/controller/declarations">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <DeclarationsQueue />;
}
