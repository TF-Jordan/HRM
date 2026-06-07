import { redirect } from "next/navigation";

import { entitledSlugs, roleHomePath } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: reachable by anyone entitled to the PAYROLL-MANAGER space
// (a multi-role user can hold it alongside their Employee space).
export default async function Layout({ children }: LayoutProps<"/[locale]/payroll-manager">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (!entitledSlugs(session.user.roles, session.user.permissions).includes("payroll-manager"))
    redirect(roleHomePath(session.user.roles, session.user.permissions));
  return <>{children}</>;
}
