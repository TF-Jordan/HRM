import * as React from "react";
import { Sidebar } from "@/components/shell/Sidebar";
import { Topbar } from "@/components/shell/Topbar";
import { ThemeProvider } from "@/components/providers/ThemeProvider";
import { getSession } from "@/server/session";
import { redirect } from "next/navigation";

export default async function AppLayout({ children }: { children: React.ReactNode }) {
  const session = await getSession();
  if (!session) {
    redirect("/login");
  }

  return (
    <ThemeProvider>
      <div className="relative z-[1] grid min-h-screen lg:grid-cols-[252px_1fr]">
        <div className="hidden lg:block">
          <Sidebar />
        </div>
        <div className="flex min-w-0 flex-col">
          <Topbar
            user={{
              displayName: session.user.displayName,
              email: session.user.email,
              role: "Admin RH",
            }}
          />
          <main className="mx-auto w-full max-w-[1440px] grow px-4 pb-20 pt-6 md:px-9 md:pt-8">
            {children}
          </main>
        </div>
      </div>
    </ThemeProvider>
  );
}
