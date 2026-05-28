import Link from "next/link";

export default function NotFound() {
  return (
    <html lang="fr">
      <body className="grid min-h-screen place-items-center bg-bg px-6 text-center">
        <div className="max-w-md">
          <p className="font-mono-tabular text-[12px] uppercase tracking-widest text-orange-600">
            404 — Page introuvable
          </p>
          <h1 className="mt-3 font-display text-[34px] font-extrabold text-ink">
            Cette page n&apos;existe pas
          </h1>
          <p className="mt-2 text-[14px] text-ink-3">
            L&apos;adresse que vous avez saisie ne mène nulle part — vérifiez l&apos;URL ou retournez à
            votre tableau de bord.
          </p>
          <Link
            href="/dashboard"
            className="mt-6 inline-flex items-center gap-2 rounded-xl bg-grad-orange px-5 py-2.5 text-[13px] font-semibold text-white shadow-orange-brand"
          >
            Retour au tableau de bord
          </Link>
        </div>
      </body>
    </html>
  );
}
