import * as React from "react";
import { Sidebar } from "@/components/shell/Sidebar";
import { Topbar } from "@/components/shell/Topbar";
import { getSession } from "@/server/session";
import { redirect } from "next/navigation";

export default async function AppLayout({ children }: { children: React.ReactNode }) {
  const session = await getSession();
  if (!session) {
    redirect("/login");
  }

  return (
    <div className="relative z-[1] grid min-h-screen grid-cols-[252px_1fr]">
      <Sidebar />
      <div className="flex min-w-0 flex-col">
        <Topbar
          user={{
            displayName: session.user.displayName,
            email: session.user.email,
            role: session.permissions[0],
          }}
        />
        <main className="mx-auto w-full max-w-[1440px] grow px-9 pb-20 pt-8">{children}</main>
      </div>
    </div>
  );
}
