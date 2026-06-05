import { ChartLine } from "lucide-react";
import { setRequestLocale } from "next-intl/server";

import { ComingSoon } from "@/components/shared/coming-soon";

export default async function Page({ params }: PageProps<"/[locale]/controller/analytics">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ComingSoon featureKey="nav.analytics" icon={ChartLine} />;
}
