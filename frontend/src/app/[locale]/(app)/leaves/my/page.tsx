import { setRequestLocale } from "next-intl/server";
import { MyLeavesClient } from "./my-leaves-client";

export default async function MyLeavesPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyLeavesClient />;
}
