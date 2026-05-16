import * as React from "react";
import { LocaleSwitcher } from "@/components/shell/LocaleSwitcher";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="relative flex min-h-screen flex-col bg-grad-ambient">
      <header className="absolute right-6 top-6 z-10 flex items-center gap-2">
        <LocaleSwitcher />
      </header>
      <main className="flex grow items-center justify-center px-6 py-16">{children}</main>
    </div>
  );
}
