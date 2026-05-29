import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { ReviewsQueue } from "@/components/reviews/reviews-queue";
import { readSession } from "@/server/session";

export default async function ReviewsIndexPage({ params }: PageProps<"/[locale]/reviews">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  const canManage =
    session.user.permissions.includes("hrm:review:manage") ||
    session.user.permissions.includes("hrm:review:create");
  if (!canManage) redirect("/reviews/mine");
  return <ReviewsQueue />;
}
