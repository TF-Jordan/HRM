import { ChevronRight } from "lucide-react";
import * as React from "react";

import { cn } from "@/lib/utils";

export type WorkflowStep = {
  key: string;
  label: string;
  state: "done" | "active" | "pending";
};

export function WorkflowStepper({
  steps,
  className,
}: {
  steps: WorkflowStep[];
  className?: string;
}) {
  return (
    <div className={cn("flex flex-wrap items-center gap-2", className)}>
      {steps.map((step, idx) => (
        <React.Fragment key={step.key}>
          <span
            className={cn(
              "rounded-full border px-3.5 py-1.5 text-[11px] font-semibold tracking-wider",
              step.state === "done" && "border-transparent bg-success-50 text-success-600",
              step.state === "active" &&
                "border-transparent bg-grad-orange text-white shadow-orange-brand",
              step.state === "pending" && "border-line bg-white text-ink-3",
            )}
          >
            {step.label}
          </span>
          {idx < steps.length - 1 && (
            <ChevronRight className="h-3.5 w-3.5 shrink-0 text-ink-4" aria-hidden="true" />
          )}
        </React.Fragment>
      ))}
    </div>
  );
}
