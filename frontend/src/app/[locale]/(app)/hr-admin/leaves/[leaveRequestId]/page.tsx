import { setRequestLocale } from "next-intl/server";

import { LeaveDetail } from "@/components/leaves/leave-detail";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/leaves/[leaveRequestId]">) {
  const { locale, leaveRequestId } = await params;
  setRequestLocale(locale);
  return <LeaveDetail leaveRequestId={leaveRequestId} />;
}
