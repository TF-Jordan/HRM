import { setRequestLocale } from "next-intl/server";

import { MyLeaves } from "@/components/leaves/my-leaves";

export default async function MyLeavesPage({ params }: PageProps<"/[locale]/leaves/my">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyLeaves />;
}
