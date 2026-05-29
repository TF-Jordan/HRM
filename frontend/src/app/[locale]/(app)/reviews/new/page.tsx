import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewReviewForm } from "@/components/reviews/new-review-form";
import { readSession } from "@/server/session";

export default async function NewReviewPage({ params }: PageProps<"/[locale]/reviews/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!session.user.permissions.includes("hrm:review:create")) {
    redirect("/reviews");
  }
  return <NewReviewForm />;
}
