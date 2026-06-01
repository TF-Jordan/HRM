"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Star } from "lucide-react";
import { useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { reviewStatusTone } from "@/lib/training-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { ReviewResponse } from "@/server/ksm/modules/reviews";

type MinePayload = { employee: EmployeeResponse | null; reviews: ReviewResponse[] };

export function MyReviews() {
  const t = useTranslations("reviews");
  const router = useRouter();

  const query = useQuery({
    queryKey: ["hrm", "reviews", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/reviews/mine"),
  });

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : query.data.reviews.length === 0 ? (
        <Card>
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("mine.empty")}</p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          {query.data.reviews.map((r) => (
            <button
              key={r.id}
              type="button"
              onClick={() => router.push(`/reviews/${r.id}`)}
              className="text-left"
            >
              <Card clickable className="h-full">
                <CardContent padding="lg">
                  <div className="flex items-center justify-between">
                    <Badge tone="orange">{r.periode}</Badge>
                    <Badge tone={reviewStatusTone(r.status)}>{t(`status.${r.status}`)}</Badge>
                  </div>
                  <div className="mt-3 flex items-center gap-3">
                    <Star className="h-5 w-5 text-orange-500" />
                    <div>
                      <div className="text-[10.5px] uppercase tracking-wider text-ink-4">
                        {t("detail.score")}
                      </div>
                      <div className="flex items-center gap-1.5">
                        <span className="font-display font-mono-tabular text-[22px] font-extrabold text-ink">
                          {r.noteGlobale != null ? Number(r.noteGlobale).toFixed(1) : "—"}
                        </span>
                        {r.noteGlobale != null && (
                          <div className="flex items-center gap-0.5">
                            {[1, 2, 3, 4, 5].map((i) => (
                              <Star
                                key={i}
                                className={cn(
                                  "h-3 w-3",
                                  i <= Math.round(Number(r.noteGlobale))
                                    ? "fill-orange-500 text-orange-500"
                                    : "fill-bg-soft text-bg-soft",
                                )}
                              />
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                  {r.commentaires && (
                    <p className="mt-3 line-clamp-3 text-[12.5px] text-ink-3">{r.commentaires}</p>
                  )}
                </CardContent>
              </Card>
            </button>
          ))}
        </div>
      )}
    </>
  );
}
