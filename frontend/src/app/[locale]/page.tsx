import { redirect } from "@/i18n/navigation";

export default async function RootPage({ params }: PageProps<"/[locale]">) {
  const { locale } = await params;
  redirect({ href: "/dashboard", locale });
}
