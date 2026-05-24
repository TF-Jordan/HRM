import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";
import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmUpdateMyPhoto } from "@/server/ksm/modules/actors";

const schema = z.object({
  photoId: uuidLike.nullable(),
});

export async function PUT(request: Request) {
  return withKsmHandler(async () => {
    const body = await parseBody(request, schema);
    const ctx = await getKsmContext();
    return ksmUpdateMyPhoto(body.photoId, ctx);
  });
}
