import "server-only";

import { callKsm } from "../client";
import type {
  CreateMedicalCertificateInput,
  CreateMedicalVisitInput,
  MedicalCertificate,
  MedicalVisit,
} from "@/lib/types/hrm/medical";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

export async function ksmCreateMedicalVisit(
  input: CreateMedicalVisitInput,
  ctx: KsmCtx,
): Promise<MedicalVisit> {
  return callKsm<MedicalVisit>(`/api/v1/hrm/medical/visits`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmGetMedicalVisit(id: string, ctx: KsmCtx): Promise<MedicalVisit> {
  return callKsm<MedicalVisit>(`/api/v1/hrm/medical/visits/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListVisitsByEmployee(
  employeeId: string,
  ctx: KsmCtx,
): Promise<MedicalVisit[]> {
  return callKsm<MedicalVisit[]>(`/api/v1/hrm/medical/employees/${employeeId}/visits`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateMedicalCertificate(
  input: CreateMedicalCertificateInput,
  ctx: KsmCtx,
): Promise<MedicalCertificate> {
  return callKsm<MedicalCertificate>(`/api/v1/hrm/medical/certificates`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmGetMedicalCertificate(
  id: string,
  ctx: KsmCtx,
): Promise<MedicalCertificate> {
  return callKsm<MedicalCertificate>(`/api/v1/hrm/medical/certificates/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListCertificatesByEmployee(
  employeeId: string,
  ctx: KsmCtx,
): Promise<MedicalCertificate[]> {
  return callKsm<MedicalCertificate[]>(`/api/v1/hrm/medical/employees/${employeeId}/certificates`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
