import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmListEmployees, ksmCreateEmployee } from "@/server/ksm/modules/employees";
import { ksmCreateActor } from "@/server/ksm/modules/actors";
import {
  ksmAdminCreateUser,
  ksmAssignRole,
  ksmListRoles,
} from "@/server/ksm/modules/admin";
import { createEmployeeSchema } from "@/lib/validation/hrm/employee.schema";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListEmployees(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const input = await parseBody(request, createEmployeeSchema);
      const ctx = await getKsmContext();

      const actor = await ksmCreateActor(
        {
          organizationId: ctx.organizationId,
          firstName: input.firstName,
          lastName: input.lastName,
          email: input.email,
          phoneNumber: input.phoneNumber,
        },
        { tenantId: ctx.tenantId, bearer: ctx.bearer },
      );

      const employee = await ksmCreateEmployee(
        {
          actorId: actor.id,
          numCnps: input.numCnps,
          categorie: input.categorie,
          echelon: input.echelon,
          dateEmbauche: input.dateEmbauche,
          departmentCode: input.departmentCode,
          modePaiement: input.modePaiement,
          compteBancaire: input.compteBancaire,
          numMobileMoney: input.numMobileMoney,
          operateurMm: input.operateurMm ?? null,
          contractType: input.contractType ?? null,
          contractDateDebut: input.contractDateDebut ?? null,
          contractDateFin: input.contractDateFin ?? null,
          salaireBase: input.salaireBase ?? null,
          avantagesNature: input.avantagesNature ?? null,
          periodeEssai: input.periodeEssai ?? null,
        },
        ctx,
      );

      let account: {
        userId: string;
        username: string;
        email: string;
        temporaryPassword: string;
        emailSent: boolean;
        roleAssigned: string | null;
        membershipCreated: boolean;
      } | null = null;
      let accountError: string | null = null;

      if (input.createAccount !== false) {
        try {
          const created = await ksmAdminCreateUser(
            {
              actorId: actor.id,
              username: employee.matricule,
              email: input.email,
              phoneNumber: input.phoneNumber ?? null,
              sendWelcomeEmail: input.sendWelcomeEmail !== false,
            },
            ctx,
          );

          let roleAssignedCode: string | null = null;
          try {
            const roles = await ksmListRoles(ctx);
            const employeRole = roles.find((r) => r.code === "EMPLOYE");
            if (employeRole) {
              await ksmAssignRole(
                {
                  userId: created.id,
                  roleId: employeRole.id,
                  scope: "TENANT",
                },
                ctx,
              );
              roleAssignedCode = "EMPLOYE";
            }
          } catch {
            // ignore — admin may lack iam:admin, account stays usable
          }

          // Best-effort org membership so the new user can complete login.
          // Failure is non-fatal: admin can complete it later from /admin/users.
          let membershipCreated = false;
          try {
            const inviteRes = await fetch(
              `${process.env.KSM_BASE_URL?.replace(/\/$/, "")}/api/employees/invite?organizationId=${ctx.organizationId}`,
              {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                  "X-Client-Id": process.env.KSM_CLIENT_ID ?? "",
                  "X-Api-Key": process.env.KSM_API_KEY ?? "",
                  "X-Tenant-Id": ctx.tenantId,
                  "X-Organization-Id": ctx.organizationId,
                  Authorization: `Bearer ${ctx.bearer}`,
                },
                body: JSON.stringify({
                  email: input.email,
                  roleId: null,
                  agencyId: null,
                  permissions: [],
                }),
              },
            );
            membershipCreated = inviteRes.ok;
          } catch {
            // ignore, not fatal
          }

          account = {
            userId: created.id,
            username: created.username,
            email: created.email,
            temporaryPassword: created.temporaryPassword,
            emailSent: created.emailSent,
            roleAssigned: roleAssignedCode,
            membershipCreated,
          };
        } catch (err) {
          accountError = (err as Error).message;
        }
      }

      return { ...employee, account, accountError };
    },
    { status: 201 },
  );
}
