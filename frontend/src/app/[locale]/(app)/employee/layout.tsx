import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the EMPLOYEE role may reach /employee/*. Anyone else is
// bounced to their own role home. KSM still enforces permissions on the API.
export default async function EmployeeLayout({ children }: LayoutProps<"/[locale]/employee">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles) !== "employee") redirect(roleHomePath(session.user.roles));
  return <>{children}</>;
}
