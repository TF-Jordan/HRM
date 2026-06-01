import { setRequestLocale } from "next-intl/server";

import { MyMedical } from "@/components/medical/my-medical";

export default async function MyMedicalPage({
  params,
}: PageProps<"/[locale]/medical/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyMedical />;
}
