import { cn } from "@/lib/utils";

export function SectionTitle({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "my-4 flex items-center gap-3 text-[11px] font-bold uppercase tracking-[0.1em] text-ink-3",
        "before:block before:h-[2px] before:w-[18px] before:rounded-full before:bg-grad-orange before:content-['']",
        "after:flex-1 after:bg-[linear-gradient(90deg,var(--color-line)_0%,transparent_100%)] after:h-px after:content-['']",
        className,
      )}
    >
      <span>{children}</span>
    </div>
  );
}
