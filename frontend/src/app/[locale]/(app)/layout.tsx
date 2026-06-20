import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { SessionProvider, type ClientSession } from "@/components/providers/session-provider";
import { ForcePasswordChangeGate } from "@/components/auth/force-password-change-gate";
import { HrmReconnectBanner } from "@/components/auth/hrm-reconnect-banner";
import { Sidebar } from "@/components/shell/sidebar";
import { Topbar } from "@/components/shell/topbar";
import { readSession } from "@/server/session";

export default async function AppLayout({
  children,
  params,
}: LayoutProps<"/[locale]">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) {
    redirect("/login");
  }

  const clientSession: ClientSession = {
    user: session.user,
    workspace: session.workspace,
    forcePasswordChange: session.forcePasswordChange === true,
    hrmNeedsReconnect: session.hrmNeedsReconnect === true,
    expiresAt: session.expiresAt,
  };

  return (
    <SessionProvider initialSession={clientSession}>
      <div className="relative grid min-h-screen grid-cols-[252px_1fr]">
        <Sidebar />
        <main className="flex min-w-0 flex-col">
          <Topbar />
          <HrmReconnectBanner />
          <div className="mx-auto w-full max-w-[1440px] px-9 py-8 pb-20">{children}</div>
        </main>
      </div>
      <ForcePasswordChangeGate />
    </SessionProvider>
  );
}
