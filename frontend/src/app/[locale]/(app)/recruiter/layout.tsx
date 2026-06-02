import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the RECRUITER role may reach /recruiter/*.
export default async function Layout({ children }: LayoutProps<"/[locale]/recruiter">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles, session.user.permissions) !== "recruiter") redirect(roleHomePath(session.user.roles, session.user.permissions));
  return <>{children}</>;
}
