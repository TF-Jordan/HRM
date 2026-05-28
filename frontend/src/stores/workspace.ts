"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";

import type { WorkspaceContext } from "@/lib/types/auth";

type WorkspaceState = {
  workspace: WorkspaceContext | null;
  setWorkspace: (workspace: WorkspaceContext | null) => void;
};

export const useWorkspaceStore = create<WorkspaceState>()(
  persist(
    (set) => ({
      workspace: null,
      setWorkspace: (workspace) => set({ workspace }),
    }),
    { name: "hrm.workspace" },
  ),
);
