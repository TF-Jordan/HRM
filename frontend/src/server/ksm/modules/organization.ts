import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type OrganizationResponse = {
  id: string;
  tenantId: string;
  businessActorId: string;
  governanceStatus?: string;
  governedByUserId?: string | null;
  governedAt?: string | null;
  governanceReason?: string | null;
  code: string;
  service: string;
  organizationType: string;
  isIndividualBusiness: boolean;
  email?: string | null;
  shortName: string;
  longName: string;
  displayName: string;
  legalName: string;
  description?: string | null;
  logoUri?: string | null;
  logoId?: string | null;
  websiteUrl?: string | null;
  socialNetwork?: string | null;
  businessRegistrationNumber?: string | null;
  taxNumber?: string | null;
  capitalShare?: number | string | null;
  ceoName?: string | null;
  yearFounded?: number | null;
  keywords?: string[];
  numberOfEmployees?: number | null;
  legalForm?: string | null;
  isActive: boolean;
  status?: string;
};

export type UpdateOrganizationRequest = Partial<{
  code: string;
  service: string; // alias of organizationType
  isIndividualBusiness: boolean;
  email: string;
  shortName: string;
  longName: string;
  description: string;
  logoUri: string;
  logoId: string;
  websiteUrl: string;
  socialNetwork: string;
  businessRegistrationNumber: string;
  taxNumber: string;
  capitalShare: number;
  ceoName: string;
  yearFounded: number;
  keywords: string[];
  numberOfEmployees: number;
  legalForm: string;
  isActive: boolean;
  status: string;
}>;

export type AgencyResponse = {
  id: string;
  tenantId: string;
  organizationId: string;
  code: string;
  name: string;
  agencyType: string;
  active: boolean;
  isHeadquarter: boolean;
  city?: string | null;
  country?: string | null;
  phone?: string | null;
  email?: string | null;
  managerId?: string | null;
};

export type CreateAgencyRequest = {
  code: string;
  name: string;
  agencyType?: string;
  isHeadquarter?: boolean;
  city?: string;
  country?: string;
  phone?: string;
  email?: string;
  managerId?: string;
  active?: boolean;
};

export function getOrganization(organizationId: string, session: AppSession) {
  return callKsm<OrganizationResponse>(
    `/api/organizations/${organizationId}`,
    {},
    { session },
  );
}

export function updateOrganization(
  organizationId: string,
  body: UpdateOrganizationRequest,
  session: AppSession,
) {
  return callKsm<OrganizationResponse>(
    `/api/organizations/${organizationId}`,
    { method: "PATCH", body },
    { session },
  );
}

export function suspendOrganization(organizationId: string, reason: string | undefined, session: AppSession) {
  return callKsm<OrganizationResponse>(
    `/api/organizations/${organizationId}/suspend`,
    { method: "POST", body: { reason } },
    { session },
  );
}

export function closeOrganization(organizationId: string, reason: string | undefined, session: AppSession) {
  return callKsm<OrganizationResponse>(
    `/api/organizations/${organizationId}/close`,
    { method: "POST", body: { reason } },
    { session },
  );
}

export function reopenOrganization(organizationId: string, reason: string | undefined, session: AppSession) {
  return callKsm<OrganizationResponse>(
    `/api/organizations/${organizationId}/reopen`,
    { method: "POST", body: { reason } },
    { session },
  );
}

export function listAgencies(organizationId: string, session: AppSession) {
  return callKsm<AgencyResponse[]>(
    `/api/organizations/${organizationId}/agencies`,
    {},
    { session },
  );
}

export function createAgency(
  organizationId: string,
  body: CreateAgencyRequest,
  session: AppSession,
) {
  return callKsm<AgencyResponse>(
    `/api/organizations/${organizationId}/agencies`,
    { method: "POST", body },
    { session },
  );
}
