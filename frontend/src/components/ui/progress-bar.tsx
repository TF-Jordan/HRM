import { cn } from "@/lib/utils";

export function ProgressBar({
  value,
  max = 100,
  size = "md",
  className,
}: {
  value: number;
  max?: number;
  size?: "sm" | "md" | "lg";
  className?: string;
}) {
  const pct = Math.max(0, Math.min(100, (value / max) * 100));
  return (
    <div
      role="progressbar"
      aria-valuenow={value}
      aria-valuemin={0}
      aria-valuemax={max}
      className={cn(
        "w-full overflow-hidden rounded-full bg-bg-soft shadow-[inset_0_1px_2px_rgba(15,11,5,0.06)]",
        size === "sm" && "h-[5px]",
        size === "md" && "h-2",
        size === "lg" && "h-3",
        className,
      )}
    >
      <div
        className="h-full rounded-full bg-grad-orange shadow-[0_0_12px_rgba(242,107,15,0.4)] transition-[width] duration-700 ease-[var(--ease-brand)]"
        style={{ width: `${pct}%` }}
      />
    </div>
  );
}
