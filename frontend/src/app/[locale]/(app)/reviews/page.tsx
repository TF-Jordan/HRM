import { setRequestLocale } from "next-intl/server";
import { ReviewsClient } from "./reviews-client";

export default async function ReviewsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ReviewsClient />;
}
