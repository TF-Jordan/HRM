import { setRequestLocale } from "next-intl/server";

import { NewReviewForm } from "@/components/reviews/new-review-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/reviews/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewReviewForm />;
}
