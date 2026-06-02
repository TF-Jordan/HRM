import { setRequestLocale } from "next-intl/server";

import { MyProfile } from "@/components/profile/my-profile";

export default async function Page({ params }: PageProps<"/[locale]/employee/profile">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyProfile />;
}
