import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the DRH role may reach /drh/*.
export default async function Layout({ children }: LayoutProps<"/[locale]/drh">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles, session.user.permissions) !== "drh") redirect(roleHomePath(session.user.roles, session.user.permissions));
  return <>{children}</>;
}
