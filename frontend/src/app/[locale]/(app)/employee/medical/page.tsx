import { setRequestLocale } from "next-intl/server";

import { MyMedical } from "@/components/medical/my-medical";

export default async function Page({ params }: PageProps<"/[locale]/employee/medical">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyMedical />;
}
