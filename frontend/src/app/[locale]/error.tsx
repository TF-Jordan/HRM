"use client";

import * as React from "react";

import { Button } from "@/components/ui/button";

export default function LocaleError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  React.useEffect(() => {
    // eslint-disable-next-line no-console
    console.error(error);
  }, [error]);

  return (
    <div className="grid min-h-[60vh] place-items-center px-6 text-center">
      <div className="max-w-md">
        <p className="font-mono-tabular text-[12px] uppercase tracking-widest text-orange-600">
          Une erreur est survenue
        </p>
        <h1 className="mt-3 font-display text-[28px] font-extrabold text-ink">
          Quelque chose s&apos;est mal passé
        </h1>
        <p className="mt-2 text-[14px] text-ink-3">
          {error.message || "Une erreur inattendue s'est produite. Réessayez ou contactez le support."}
        </p>
        <Button onClick={reset} className="mt-6">
          Réessayer
        </Button>
      </div>
    </div>
  );
}
