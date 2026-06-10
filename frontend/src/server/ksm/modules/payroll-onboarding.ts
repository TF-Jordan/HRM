import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

/**
 * Self-describing payroll-core capabilities consumed by administration-core's onboarding
 * pipeline. Exposed by the BFF mainly so admin tooling on the frontend can introspect what
 * payroll offers; the production caller is administration-core itself, server-to-server.
 */

export type PayrollOnboardingManifest = {
  module: "payroll";
  version: string;
  permissions: { code: string; label: string; description: string }[];
  suggestedRoleTemplates: {
    code: string;
    name: string;
    description: string;
    scopeType: string;
    permissions: string[];
  }[];
  dataSourceModes: { code: string; label: string; description: string }[];
  onboardingFlow: {
    steps: { order: number; description: string; endpoint: string; method: string }[];
  };
};

export function getPayrollOnboardingManifest(session: AppSession) {
  return callKsm<PayrollOnboardingManifest>(
    "/api/v1/payroll/onboarding/manifest",
    {},
    { session },
  );
}
