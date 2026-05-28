import { setRequestLocale } from "next-intl/server";

import { Sidebar } from "@/components/shell/sidebar";
import { Topbar } from "@/components/shell/topbar";

// TEMP — real user/session will be injected after Phase 1 (auth).
const DEMO_USER = { name: "Jordan Toulépi", role: "SuperAdmin" };

export default async function AppLayout({
  children,
  params,
}: LayoutProps<"/[locale]">) {
  const { locale } = await params;
  setRequestLocale(locale);

  return (
    <div className="relative grid min-h-screen grid-cols-[252px_1fr]">
      <Sidebar user={DEMO_USER} />
      <main className="flex min-w-0 flex-col">
        <Topbar user={DEMO_USER} notificationsCount={3} />
        <div className="mx-auto w-full max-w-[1440px] px-9 py-8 pb-20">{children}</div>
      </main>
    </div>
  );
}
