import { setRequestLocale } from "next-intl/server";

import { MySkills } from "@/components/skills/my-skills";

export default async function Page({ params }: PageProps<"/[locale]/employee/skills">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MySkills />;
}
