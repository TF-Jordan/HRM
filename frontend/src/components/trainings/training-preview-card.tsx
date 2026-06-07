"use client";

import { Building2, CalendarRange, MapPin } from "lucide-react";
import { useTranslations } from "next-intl";

import { formatDate, formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";

/** Inclusive day count between two ISO dates, or null when not computable. */
function durationDays(start?: string | null, end?: string | null): number | null {
  if (!start || !end) return null;
  const s = new Date(start);
  const e = new Date(end);
  if (Number.isNaN(s.getTime()) || Number.isNaN(e.getTime()) || e < s) return null;
  return Math.round((e.getTime() - s.getTime()) / 86_400_000) + 1;
}

export type TrainingPreviewCardProps = {
  /** Small uppercase label in the orange header (e.g. the status or "Preview"). */
  headerLabel: string;
  title: string;
  organisme?: string | null;
  lieu?: string | null;
  dateDebut?: string | null;
  dateFin?: string | null;
  cout?: number | null;
  nbPlaces?: number | null;
  locale: "fr" | "en";
  onClick?: () => void;
  className?: string;
};

/**
 * The "session preview" card shared by the create form (live preview) and the
 * catalog grid, so a planned session looks identical everywhere: orange header
 * with label + title, then provider / location / period rows and a stats strip
 * (duration · seats · cost).
 */
export function TrainingPreviewCard({
  headerLabel,
  title,
  organisme,
  lieu,
  dateDebut,
  dateFin,
  cout,
  nbPlaces,
  locale,
  onClick,
  className,
}: TrainingPreviewCardProps) {
  const t = useTranslations("trainings");
  const days = durationDays(dateDebut, dateFin);
  const period = dateDebut
    ? `${formatDate(dateDebut, { locale })}${dateFin ? ` → ${formatDate(dateFin, { locale })}` : ""}`
    : "—";

  const card = (
    <div
      className={cn(
        "h-full overflow-hidden rounded-[18px] border border-line bg-white shadow-xs-brand",
        onClick &&
          "cursor-pointer transition-all hover:-translate-y-0.5 hover:shadow-md-brand",
        className,
      )}
    >
      <div className="bg-grad-orange px-5 py-4 text-white">
        <p className="text-[11px] font-semibold uppercase tracking-[0.08em] text-white/80">
          {headerLabel}
        </p>
        <p className="mt-1 line-clamp-2 font-display text-[16px] font-bold leading-snug">
          {title}
        </p>
      </div>
      <div className="space-y-3 p-5">
        <Row icon={Building2} value={organisme?.trim() || "—"} />
        <Row icon={MapPin} value={lieu?.trim() || "—"} />
        <Row icon={CalendarRange} value={period} />
        <div className="grid grid-cols-2 gap-3 border-t border-line-soft pt-3">
          <Stat
            label={t("new.preview.duration")}
            value={days != null ? t("card.durationDays", { days }) : "—"}
          />
          <Stat
            label={t("new.fields.nbPlaces")}
            value={nbPlaces != null && nbPlaces > 0 ? String(nbPlaces) : "—"}
          />
          <Stat
            label={t("new.fields.cout")}
            value={cout != null && cout > 0 ? `${formatNumber(cout, locale)} XAF` : t("card.free")}
            wide
          />
        </div>
      </div>
    </div>
  );

  if (onClick) {
    return (
      <button type="button" onClick={onClick} className="block w-full text-left">
        {card}
      </button>
    );
  }
  return card;
}

function Row({
  icon: Icon,
  value,
}: {
  icon: React.ComponentType<{ className?: string }>;
  value: string;
}) {
  return (
    <div className="flex items-center gap-2.5 text-[12.5px] text-ink-2">
      <Icon className="h-3.5 w-3.5 shrink-0 text-ink-4" />
      <span className="truncate">{value}</span>
    </div>
  );
}

function Stat({ label, value, wide }: { label: string; value: string; wide?: boolean }) {
  return (
    <div className={cn(wide && "col-span-2")}>
      <div className="text-[10px] uppercase tracking-wider text-ink-4">{label}</div>
      <div className="font-mono-tabular text-[13.5px] font-bold text-ink">{value}</div>
    </div>
  );
}
