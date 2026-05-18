import "server-only";

import { callKsm } from "../client";
import type {
  CreateEmployeeSkillInput,
  CreateSkillInput,
  EmployeeSkill,
  Skill,
} from "@/lib/types/hrm/skill";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

export async function ksmListSkills(ctx: KsmCtx): Promise<Skill[]> {
  return callKsm<Skill[]>(`/api/v1/hrm/skills`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmGetSkill(id: string, ctx: KsmCtx): Promise<Skill> {
  return callKsm<Skill>(`/api/v1/hrm/skills/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateSkill(input: CreateSkillInput, ctx: KsmCtx): Promise<Skill> {
  return callKsm<Skill>(`/api/v1/hrm/skills`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListEmployeeSkills(
  employeeId: string,
  ctx: KsmCtx,
): Promise<EmployeeSkill[]> {
  return callKsm<EmployeeSkill[]>(`/api/v1/hrm/skills/employees/${employeeId}/skills`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateEmployeeSkill(
  input: CreateEmployeeSkillInput,
  ctx: KsmCtx,
): Promise<EmployeeSkill> {
  return callKsm<EmployeeSkill>(`/api/v1/hrm/skills/employee-skills`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
