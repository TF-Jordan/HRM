"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { LogOut, User, Settings as SettingsIcon } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Link, useRouter } from "@/i18n/navigation";

export type UserChipProps = {
  displayName: string;
  email?: string;
  matricule?: string | null;
  role?: string;
};

export function UserChip({ displayName, matricule, role }: UserChipProps) {
  const tCommon = useTranslations("common");
  const tActions = useTranslations("common.actions" as never);
  const router = useRouter();

  const onLogout = async () => {
    await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
    router.replace("/login");
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger className="flex items-center gap-2.5 rounded-full border border-line bg-white py-1 pl-3.5 pr-1 shadow-elev-sm transition-all hover:-translate-y-px hover:border-line-strong">
        <div className="hidden flex-col items-start leading-tight sm:flex">
          <span className="text-[13px] font-semibold text-ink">{displayName}</span>
          {matricule && (
            <span className="font-mono text-[10.5px] text-ink-4">{matricule}</span>
          )}
          {role && <span className="text-[11px] text-ink-3">{role}</span>}
        </div>
        <Avatar name={displayName} />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuLabel>{tCommon("profile")}</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem asChild>
          <Link href={"/account/profile" as never}>
            <User className="size-4" />
            <span>{tCommon("profile")}</span>
          </Link>
        </DropdownMenuItem>
        <DropdownMenuItem asChild>
          <Link href={"/settings" as never}>
            <SettingsIcon className="size-4" />
            <span>{tCommon("settings")}</span>
          </Link>
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={onLogout}>
          <LogOut className="size-4" />
          <span>{tActions("logout") as string}</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
