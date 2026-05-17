import { setRequestLocale } from "next-intl/server";
import { ReviewDetailClient } from "./review-detail-client";

export default async function ReviewDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <ReviewDetailClient id={id} />;
}
