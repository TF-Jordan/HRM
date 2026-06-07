import { redirect } from "next/navigation";

import { entitledSlugs, roleHomePath } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: every entitled user (incl. privileged staff, who are also
// employees) may reach their Employee self-service space. KSM still enforces
// permissions on the API.
export default async function EmployeeLayout({ children }: LayoutProps<"/[locale]/employee">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (!entitledSlugs(session.user.roles, session.user.permissions).includes("employee"))
    redirect(roleHomePath(session.user.roles, session.user.permissions));
  return <>{children}</>;
}
