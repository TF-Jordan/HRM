import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the HR-ADMIN role may reach /hr-admin/*.
export default async function Layout({ children }: LayoutProps<"/[locale]/hr-admin">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles) !== "hr-admin") redirect(roleHomePath(session.user.roles));
  return <>{children}</>;
}
