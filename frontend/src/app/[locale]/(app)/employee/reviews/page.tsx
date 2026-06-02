import { setRequestLocale } from "next-intl/server";

import { MyReviews } from "@/components/reviews/my-reviews";

export default async function Page({ params }: PageProps<"/[locale]/employee/reviews">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyReviews />;
}
