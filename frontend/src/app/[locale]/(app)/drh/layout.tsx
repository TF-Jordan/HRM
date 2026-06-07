import { redirect } from "next/navigation";

import { entitledSlugs, roleHomePath } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: reachable by anyone entitled to the DRH space.
export default async function Layout({ children }: LayoutProps<"/[locale]/drh">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (!entitledSlugs(session.user.roles, session.user.permissions).includes("drh"))
    redirect(roleHomePath(session.user.roles, session.user.permissions));
  return <>{children}</>;
}
