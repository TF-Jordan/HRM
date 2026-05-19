import { redirect } from "next/navigation";

export default function LoansIndexPage() {
  redirect("/loans/approve" as never);
}
