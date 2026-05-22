"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch } from "@/lib/api-client";
import type {
  AdminCreateUserInput,
  AdminCreateUserResponse,
  AssignRoleInput,
  CreateRoleInput,
  Role,
  UpdateRoleInput,
  UserRoleAssignment,
  UserSummary,
} from "@/lib/types/admin";

// ----- Roles -----

export function useRoles() {
  return useQuery({
    queryKey: ["admin", "roles"],
    queryFn: () => bffFetch<Role[]>("/api/admin/roles"),
  });
}

export function useRole(id: string | undefined) {
  return useQuery({
    queryKey: ["admin", "roles", id],
    queryFn: () => bffFetch<Role>(`/api/admin/roles/${id}`),
    enabled: !!id,
  });
}

export function useCreateRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateRoleInput) =>
      bffFetch<Role>("/api/admin/roles", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin", "roles"] }),
  });
}

export function useUpdateRole(id: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: UpdateRoleInput) =>
      bffFetch<Role>(`/api/admin/roles/${id}`, { method: "PUT", body: JSON.stringify(input) }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin", "roles"] });
      if (id) qc.invalidateQueries({ queryKey: ["admin", "roles", id] });
    },
  });
}

export function useDeleteRole() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => bffFetch<void>(`/api/admin/roles/${id}`, { method: "DELETE" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin", "roles"] }),
  });
}

// ----- Assignments -----

export function useUserAssignments(userId: string | undefined) {
  return useQuery({
    queryKey: ["admin", "assignments", "by-user", userId],
    queryFn: () =>
      bffFetch<UserRoleAssignment[]>(`/api/admin/users/${userId}/assignments`),
    enabled: !!userId,
  });
}

export function useRoleAssignments(roleId: string | undefined) {
  return useQuery({
    queryKey: ["admin", "assignments", "by-role", roleId],
    queryFn: () =>
      bffFetch<UserRoleAssignment[]>(`/api/admin/roles/${roleId}/assignments`),
    enabled: !!roleId,
  });
}

export function useAssignRole(userId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: AssignRoleInput) =>
      bffFetch<UserRoleAssignment>("/api/admin/assignments", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      if (userId)
        qc.invalidateQueries({ queryKey: ["admin", "assignments", "by-user", userId] });
    },
  });
}

export function useRevokeAssignment(userId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      bffFetch<void>(`/api/admin/assignments/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      if (userId)
        qc.invalidateQueries({ queryKey: ["admin", "assignments", "by-user", userId] });
    },
  });
}

// ----- Users -----

export function useUsers() {
  return useQuery({
    queryKey: ["admin", "users"],
    queryFn: () => bffFetch<UserSummary[]>("/api/admin/users"),
  });
}

export function useAdminCreateUser() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: AdminCreateUserInput) =>
      bffFetch<AdminCreateUserResponse>("/api/admin/users", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["admin", "users"] }),
  });
}
