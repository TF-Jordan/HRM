import { redirect } from "next/navigation";

export default function LeavesIndexPage() {
  redirect("/leaves/pending" as never);
}
