import { setRequestLocale } from "next-intl/server";

import { MyLeaves } from "@/components/leaves/my-leaves";

export default async function Page({ params }: PageProps<"/[locale]/employee/leaves">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyLeaves />;
}
