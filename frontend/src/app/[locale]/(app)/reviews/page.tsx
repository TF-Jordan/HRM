import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { ReviewsQueue } from "@/components/reviews/reviews-queue";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function ReviewsIndexPage({ params }: PageProps<"/[locale]/reviews">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  if (!hasPermission(session, ["hrm:review:manage", "hrm:review:create"])) redirect("/reviews/mine");
  return <ReviewsQueue />;
}
