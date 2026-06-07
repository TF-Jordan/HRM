import { redirect } from "next/navigation";

import { entitledSlugs, roleHomePath } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: reachable by anyone entitled to the MANAGER space. KSM still
// enforces permissions on the API.
export default async function ManagerLayout({ children }: LayoutProps<"/[locale]/manager">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (!entitledSlugs(session.user.roles, session.user.permissions).includes("manager"))
    redirect(roleHomePath(session.user.roles, session.user.permissions));
  return <>{children}</>;
}
