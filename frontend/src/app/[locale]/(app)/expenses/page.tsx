import { redirect } from "next/navigation";

export default function ExpensesIndexPage() {
  redirect("/expenses/approve" as never);
}
