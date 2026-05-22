import { setRequestLocale } from "next-intl/server";
import { MedicalClient } from "./medical-client";
import { getProfileSummary } from "@/server/profile";

export default async function MedicalPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const profile = await getProfileSummary();
  const canManage = profile?.permissions.includes("hrm:medical:create") ?? false;
  return (
    <MedicalClient
      mode={canManage ? "manager" : "self"}
      selfEmployeeId={profile?.employeeId ?? null}
    />
  );
}
