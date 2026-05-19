import { setRequestLocale } from "next-intl/server";
import { ComingSoon } from "@/components/shell/ComingSoon";

export default async function SettingsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return (
    <ComingSoon
      titleKey="items.settings"
      hint="Paramètres tenant / organisation / agences — module à venir."
    />
  );
}
