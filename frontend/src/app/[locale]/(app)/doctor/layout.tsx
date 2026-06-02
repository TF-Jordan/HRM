import { redirect } from "next/navigation";

import { roleHomePath, roleSlug } from "@/lib/roles";
import { readSession } from "@/server/session";

// Namespace guard: only the DOCTOR role may reach /doctor/*.
export default async function Layout({ children }: LayoutProps<"/[locale]/doctor">) {
  const session = await readSession();
  if (!session) redirect("/login");
  if (roleSlug(session.user.roles) !== "doctor") redirect(roleHomePath(session.user.roles));
  return <>{children}</>;
}
