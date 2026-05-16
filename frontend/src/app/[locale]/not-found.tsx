import { setRequestLocale, getTranslations } from "next-intl/server";
import { Button } from "@/components/ui/button";
import { Link } from "@/i18n/navigation";

export default async function NotFoundPage() {
  setRequestLocale("fr");
  const t = await getTranslations("errors.notFoundPage");
  return (
    <div className="flex min-h-screen items-center justify-center bg-grad-ambient px-6">
      <div className="max-w-md text-center">
        <div className="font-display text-7xl font-extrabold text-glow-orange">404</div>
        <h1 className="mt-2 font-display text-3xl font-bold text-ink">{t("title")}</h1>
        <p className="mt-3 text-[14px] text-ink-3">{t("subtitle")}</p>
        <div className="mt-6 flex justify-center">
          <Button asChild>
            <Link href={"/dashboard" as never}>{t("backHome")}</Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
