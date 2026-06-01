import { setRequestLocale } from "next-intl/server";

import { LeaveDetail } from "@/components/leaves/leave-detail";

export default async function LeaveDetailPage({
  params,
}: PageProps<"/[locale]/leaves/[leaveRequestId]">) {
  const { locale, leaveRequestId } = await params;
  setRequestLocale(locale);
  return <LeaveDetail leaveRequestId={leaveRequestId} />;
}
