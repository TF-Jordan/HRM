import { redirect } from "next/navigation";

import { entitledSlugs, roleHomePath } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: reachable by anyone entitled to the ADMIN space.
export default async function Layout({ children }: LayoutProps<"/[locale]/admin">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (!entitledSlugs(session.user.roles, session.user.permissions).includes("admin"))
    redirect(roleHomePath(session.user.roles, session.user.permissions));
  return <>{children}</>;
}
