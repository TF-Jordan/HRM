/**
 * Standard KSM envelope for every backend response.
 * Mirrors yowyob.comops.api.common.domain.model.ApiResponse.
 */
export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  message: string;
  errorCode: string | null;
  timestamp: string;
};

/**
 * Mapped HTTP / KSM error returned by the BFF layer.
 */
export class HttpError extends Error {
  readonly status: number;
  readonly errorCode: string | null;
  readonly upstream?: unknown;

  constructor(opts: {
    status: number;
    message: string;
    errorCode?: string | null;
    upstream?: unknown;
  }) {
    super(opts.message);
    this.name = "HttpError";
    this.status = opts.status;
    this.errorCode = opts.errorCode ?? null;
    this.upstream = opts.upstream;
  }
}

/**
 * Generic page result returned by the BFF when in-memory paginating KSM lists.
 */
export type PageResult<T> = {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
};

export type SortOrder = "asc" | "desc";

export type ListQuery = {
  page?: number;
  pageSize?: number;
  search?: string;
  sortBy?: string;
  sortOrder?: SortOrder;
};
