"use client";

import { useQuery } from "@tanstack/react-query";
import { Bell, HelpCircle, Search } from "lucide-react";
import { useTranslations } from "next-intl";

import { useSession } from "@/components/providers/session-provider";
import { LocaleSwitcher } from "@/components/shell/locale-switcher";
import { Avatar } from "@/components/ui/avatar";
import { useCan } from "@/hooks/use-can";
import { useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch } from "@/lib/api-client";
import { cn } from "@/lib/utils";

type NotificationsPayload = {
  total: number;
  buckets: { pendingAcceptance: number; declined: number; expensesToApprove: number };
};

export interface TopbarProps {
  notificationsCount?: number;
}

export function Topbar({ notificationsCount }: TopbarProps) {
  const t = useTranslations("shell.topbar");
  const tUser = useTranslations("shell.user");
  const { session } = useSession();
  const router = useRouter();
  const canManageMissions = useCan("hrm:mission:manage");
  const canAccept = useCan("hrm:mission:accept");
  const canManageExpenses = useCan("hrm:expense:manage");

  const notif = useQuery({
    queryKey: ["hrm", "notifications"],
    queryFn: () => apiFetch<NotificationsPayload>("/api/hrm/notifications"),
    enabled: !!session && (canAccept || canManageMissions || canManageExpenses),
    refetchInterval: 60_000,
  });
  const liveCount = notif.data?.total ?? 0;
  const count = notificationsCount ?? liveCount;

  function openNotifications() {
    const b = notif.data?.buckets;
    if (canManageExpenses && (b?.expensesToApprove ?? 0) > 0) {
      router.push("/expenses?status=SUBMITTED");
      return;
    }
    if (canManageMissions && (b?.declined ?? 0) > 0) {
      router.push("/mission-orders?status=DECLINED");
      return;
    }
    if (canAccept && (b?.pendingAcceptance ?? 0) > 0) {
      router.push("/mission-orders/mine");
      return;
    }
    router.push(canManageExpenses ? "/expenses" : canManageMissions ? "/mission-orders" : "/mission-orders/mine");
  }

  return (
    <header
      className={cn(
        "sticky top-0 z-20 flex items-center gap-3 px-8 py-3.5",
        "border-b border-line/70 bg-bg/70 backdrop-blur-xl backdrop-saturate-[180%]",
      )}
    >
      <div className="flex max-w-[540px] flex-1 items-center gap-2.5 rounded-xl border border-line bg-white px-4 py-2 text-ink-3 shadow-xs-brand transition-all duration-200 ease-[var(--ease-brand)] focus-within:border-orange-400 focus-within:ring-4 focus-within:ring-orange-500/12 hover:border-line-strong">
        <Search className="h-4 w-4 shrink-0" />
        <input
          type="search"
          placeholder={t("searchPlaceholder")}
          className="flex-1 border-none bg-transparent text-[13.5px] text-ink outline-none placeholder:text-ink-4"
        />
        <kbd className="hidden items-center gap-1 rounded-md border border-line bg-bg-soft px-1.5 py-0.5 font-mono-tabular text-[10px] font-semibold text-ink-3 md:inline-flex">
          ⌘K
        </kbd>
      </div>

      <div className="flex-1" />

      <LocaleSwitcher />

      <button
        type="button"
        aria-label={t("help")}
        className="grid h-[38px] w-[38px] place-items-center rounded-[11px] border border-line bg-white text-ink-2 shadow-xs-brand transition-all duration-200 hover:-translate-y-px hover:border-line-strong hover:text-ink hover:shadow-sm-brand"
      >
        <HelpCircle className="h-[18px] w-[18px]" />
      </button>

      <button
        type="button"
        aria-label={t("notifications")}
        onClick={openNotifications}
        className="relative grid h-[38px] w-[38px] place-items-center rounded-[11px] border border-line bg-white text-ink-2 shadow-xs-brand transition-all duration-200 hover:-translate-y-px hover:border-line-strong hover:text-ink hover:shadow-sm-brand"
      >
        <Bell className="h-[18px] w-[18px]" />
        {count > 0 && (
          <>
            <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-orange-500 ring-2 ring-white shadow-[0_0_0_2px_rgba(242,107,15,0.25)]" />
            <span className="absolute -right-1 -top-1 inline-flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-grad-orange px-1 text-[10px] font-bold text-white shadow-orange-brand">
              {count > 99 ? "99+" : count}
            </span>
          </>
        )}
      </button>

      {session?.user && (
        <button
          type="button"
          className="flex items-center gap-2.5 rounded-full border border-line bg-white py-1 pl-3.5 pr-1.5 shadow-xs-brand transition-all duration-200 hover:border-line-strong hover:shadow-sm-brand"
        >
          <div className="flex flex-col items-end leading-tight">
            <span className="text-[13px] font-semibold text-ink">{session.user.fullName}</span>
            <span className="text-[11px] text-ink-3">
              {tUser("role")} · {session.user.roles[0] ?? "—"}
            </span>
          </div>
          <Avatar name={session.user.fullName} size="md" />
        </button>
      )}
    </header>
  );
}
