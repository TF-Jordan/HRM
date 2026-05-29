import { setRequestLocale } from "next-intl/server";

import { MyReviews } from "@/components/reviews/my-reviews";

export default async function MyReviewsPage({ params }: PageProps<"/[locale]/reviews/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyReviews />;
}
