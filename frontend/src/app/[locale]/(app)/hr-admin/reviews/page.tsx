import { setRequestLocale } from "next-intl/server";

import { ReviewsQueue } from "@/components/reviews/reviews-queue";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/reviews">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ReviewsQueue />;
}
