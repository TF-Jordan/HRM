import "server-only";

import { callKsm } from "../client";
import type {
  Application,
  CreateApplicationInput,
  CreateJobOfferInput,
  Interview,
  JobOffer,
  OnboardingTask,
  ScheduleInterviewInput,
} from "@/lib/types/hrm/recruitment";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

const Q = (orgId: string) => ({ organizationId: orgId });

export async function ksmListJobOffers(ctx: KsmCtx): Promise<JobOffer[]> {
  return callKsm<JobOffer[]>(`/api/v1/hrm/job-offers`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: Q(ctx.organizationId),
  });
}

export async function ksmGetJobOffer(id: string, ctx: KsmCtx): Promise<JobOffer> {
  return callKsm<JobOffer>(`/api/v1/hrm/job-offers/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateJobOffer(input: CreateJobOfferInput, ctx: KsmCtx): Promise<JobOffer> {
  return callKsm<JobOffer>(`/api/v1/hrm/job-offers`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
  });
}

async function offerTx(id: string, action: string, ctx: KsmCtx): Promise<JobOffer> {
  return callKsm<JobOffer>(`/api/v1/hrm/job-offers/${id}/${action}`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
export const ksmPublishJobOffer = (id: string, ctx: KsmCtx) => offerTx(id, "publish", ctx);
export const ksmCloseJobOffer = (id: string, ctx: KsmCtx) => offerTx(id, "close", ctx);

export async function ksmListApplications(jobOfferId: string, ctx: KsmCtx): Promise<Application[]> {
  return callKsm<Application[]>(`/api/v1/hrm/job-offers/${jobOfferId}/applications`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmGetApplication(id: string, ctx: KsmCtx): Promise<Application> {
  return callKsm<Application>(`/api/v1/hrm/applications/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateApplication(input: CreateApplicationInput, ctx: KsmCtx): Promise<Application> {
  return callKsm<Application>(`/api/v1/hrm/applications`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

async function appTx(id: string, action: string, ctx: KsmCtx): Promise<Application> {
  return callKsm<Application>(`/api/v1/hrm/applications/${id}/${action}`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
export const ksmShortlistApp = (id: string, ctx: KsmCtx) => appTx(id, "shortlist", ctx);
export const ksmInterviewApp = (id: string, ctx: KsmCtx) => appTx(id, "interview", ctx);
export const ksmOfferApp = (id: string, ctx: KsmCtx) => appTx(id, "offer", ctx);
export const ksmRejectApp = (id: string, ctx: KsmCtx) => appTx(id, "reject", ctx);
export const ksmHireApp = (id: string, ctx: KsmCtx) => appTx(id, "hire", ctx);

export async function ksmListInterviews(applicationId: string, ctx: KsmCtx): Promise<Interview[]> {
  return callKsm<Interview[]>(`/api/v1/hrm/applications/${applicationId}/interviews`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmScheduleInterview(input: ScheduleInterviewInput, ctx: KsmCtx): Promise<Interview> {
  return callKsm<Interview>(`/api/v1/hrm/interviews`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListOnboardingTasks(employeeId: string, ctx: KsmCtx): Promise<OnboardingTask[]> {
  return callKsm<OnboardingTask[]>(`/api/v1/hrm/onboarding-tasks/employee/${employeeId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
