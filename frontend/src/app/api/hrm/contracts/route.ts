import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListEmployees } from "@/server/ksm/modules/employees";
import { ksmListContracts } from "@/server/ksm/modules/contracts";
import type { ContractWithEmployee } from "@/lib/types/hrm/contract";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    const employees = await ksmListEmployees(ctx);
    const nested = await Promise.all(
      employees.map(async (e) => {
        try {
          const contracts = await ksmListContracts(e.id, ctx);
          return contracts.map<ContractWithEmployee>((c) => ({
            ...c,
            employeeName: e.actorDisplayName,
            employeeMatricule: e.matricule,
            employeeDepartment: e.departmentCode,
          }));
        } catch {
          return [];
        }
      }),
    );
    return nested.flat();
  });
}
