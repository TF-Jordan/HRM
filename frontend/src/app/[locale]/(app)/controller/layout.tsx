import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the CONTROLLER role may reach /controller/*.
export default async function Layout({ children }: LayoutProps<"/[locale]/controller">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles) !== "controller") redirect(roleHomePath(session.user.roles));
  return <>{children}</>;
}
