import { setRequestLocale } from "next-intl/server";

import { MyOnboarding } from "@/components/recruitment/my-onboarding";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/recruitment/onboarding">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyOnboarding />;
}
