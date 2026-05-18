import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler, parseBody } from "@/server/ksm/handler";
import { ksmCreateSkill, ksmListSkills } from "@/server/ksm/modules/skills";
import { createSkillSchema } from "@/lib/validation/hrm/skill.schema";

export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    return ksmListSkills(ctx);
  });
}

export async function POST(request: Request) {
  return withKsmHandler(
    async () => {
      const body = await parseBody(request, createSkillSchema);
      const ctx = await getKsmContext();
      return ksmCreateSkill(
        {
          name: body.name,
          categorie: body.categorie ?? null,
          description: body.description ?? null,
        },
        ctx,
      );
    },
    { status: 201 },
  );
}
