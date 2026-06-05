import { ClipboardList } from "lucide-react";
import { setRequestLocale } from "next-intl/server";

import { ComingSoon } from "@/components/shared/coming-soon";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/payroll-variables">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ComingSoon featureKey="nav.variables" icon={ClipboardList} />;
}
