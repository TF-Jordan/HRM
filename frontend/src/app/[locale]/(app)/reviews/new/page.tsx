import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewReviewForm } from "@/components/reviews/new-review-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewReviewPage({ params }: PageProps<"/[locale]/reviews/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:review:create")) redirect("/reviews");
  return <NewReviewForm />;
}
