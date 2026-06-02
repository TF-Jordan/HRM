import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the MANAGER role may reach /manager/*. Anyone else is
// bounced to their own role home. KSM still enforces permissions on the API.
export default async function ManagerLayout({ children }: LayoutProps<"/[locale]/manager">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles) !== "manager") redirect(roleHomePath(session.user.roles));
  return <>{children}</>;
}
