import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the PAYROLL-MANAGER role may reach /payroll-manager/*.
export default async function Layout({ children }: LayoutProps<"/[locale]/payroll-manager">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles) !== "payroll-manager") redirect(roleHomePath(session.user.roles));
  return <>{children}</>;
}
