import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateDeclaration, ksmListDeclarations } from "@/server/ksm/modules/declarations";
import { createDeclarationSchema } from "@/lib/validation/hrm/declaration.schema";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListDeclarations(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createDeclarationSchema);
      const ctx = await getKsmContext();
      return ksmCreateDeclaration(body, ctx);
    },
    { status: 201 },
  );
}
