import * as React from "react";
import { ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

export type WorkflowStep = {
  key: string;
  label: string;
  state: "done" | "active" | "todo";
};

export type WorkflowTimelineProps = {
  steps: WorkflowStep[];
  className?: string;
};

export function WorkflowTimeline({ steps, className }: WorkflowTimelineProps) {
  return (
    <div
      className={cn("flex flex-wrap items-center gap-2", className)}
      role="list"
      aria-label="Workflow"
    >
      {steps.map((step, i) => (
        <React.Fragment key={step.key}>
          <span
            role="listitem"
            className={cn(
              "rounded-full border px-3 py-1.5 text-[11px] font-semibold tracking-wide",
              step.state === "done" && "border-transparent bg-status-green-50 text-status-green-600",
              step.state === "active" &&
                "border-transparent bg-grad-orange text-white shadow-brand",
              step.state === "todo" && "border-line bg-white text-ink-3",
            )}
          >
            {step.label}
          </span>
          {i < steps.length - 1 && <ChevronRight className="size-3.5 text-ink-4" aria-hidden />}
        </React.Fragment>
      ))}
    </div>
  );
}
