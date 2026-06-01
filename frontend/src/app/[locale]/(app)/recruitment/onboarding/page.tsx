import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { MyOnboarding } from "@/components/recruitment/my-onboarding";
import { readSession } from "@/server/session";

export default async function MyOnboardingPage({
  params,
}: PageProps<"/[locale]/recruitment/onboarding">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  return <MyOnboarding />;
}
