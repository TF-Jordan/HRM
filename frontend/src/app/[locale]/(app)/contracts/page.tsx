import { setRequestLocale } from "next-intl/server";
import { ComingSoon } from "@/components/shell/ComingSoon";

export default async function ContractsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return (
    <ComingSoon
      titleKey="items.contracts"
      hint="Pour le moment, les contrats se consultent depuis la fiche employé (Employés → cliquer un employé → onglet Contrats)."
    />
  );
}
