import { setRequestLocale } from "next-intl/server";

export default async function AuthLayout({
  children,
  params,
}: LayoutProps<"/[locale]">) {
  const { locale } = await params;
  setRequestLocale(locale);

  return (
    <div className="relative grid min-h-screen place-items-center px-6 py-12">
      <span
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(900px_circle_at_50%_-10%,rgba(242,107,15,0.18)_0%,transparent_55%),radial-gradient(700px_circle_at_15%_90%,rgba(252,211,77,0.15)_0%,transparent_55%)]"
      />
      <div className="relative w-full max-w-[460px]">
        <div className="mb-8 flex flex-col items-center gap-3 text-center">
          <span className="relative grid h-16 w-16 place-items-center rounded-2xl bg-grad-orange font-display text-3xl font-extrabold text-white shadow-orange-lg-brand before:absolute before:inset-1 before:rounded-[14px] before:bg-[linear-gradient(180deg,rgba(255,255,255,0.3),transparent_50%)] before:pointer-events-none">
            H
          </span>
        </div>
        {children}
      </div>
    </div>
  );
}
