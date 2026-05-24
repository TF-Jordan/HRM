import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateReview, ksmListReviews } from "@/server/ksm/modules/reviews";
import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

const createBody = z.object({
  employeeId: uuidLike,
  evaluateurPartyId: uuidLike,
  evaluateurDisplayName: z.string().trim().min(1),
  periode: z.string().trim().min(4),
});

export async function GET(request: Request) {
  const url = new URL(request.url);
  const periode = url.searchParams.get("periode");
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListReviews(periode, ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createBody);
      const ctx = await getKsmContext();
      return ksmCreateReview(body, ctx);
    },
    { status: 201 },
  );
}
