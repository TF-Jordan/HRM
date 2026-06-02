import { setRequestLocale } from "next-intl/server";

import { ReviewDetail } from "@/components/reviews/review-detail";

export default async function Page({ params }: PageProps<"/[locale]/controller/reviews/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <ReviewDetail reviewId={id} />;
}
