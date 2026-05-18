"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Menu } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Sidebar } from "./Sidebar";

export function MobileSidebar() {
  const [open, setOpen] = React.useState(false);
  const tCommon = useTranslations("common");
  return (
    <>
      <button
        type="button"
        aria-label={tCommon("openMenu")}
        onClick={() => setOpen(true)}
        className="grid size-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-elev-sm transition-all hover:border-line-strong hover:text-ink lg:hidden dark:border-dark-line dark:bg-dark-2 dark:text-ink"
      >
        <Menu className="size-4" />
      </button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-[260px] p-0">
          <DialogHeader className="sr-only">
            <DialogTitle>{tCommon("openMenu")}</DialogTitle>
          </DialogHeader>
          <div onClick={() => setOpen(false)}>
            <Sidebar />
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
