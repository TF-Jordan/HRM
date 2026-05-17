import { setRequestLocale } from "next-intl/server";
import { MyProfileClient } from "./my-profile-client";

export default async function MyProfilePage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyProfileClient />;
}
