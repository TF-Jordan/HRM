/**
 * Standard KSM API response envelope.
 * See ANALYSE_KSM_HRM.md § 1.4.
 */
export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  message: string;
  errorCode: string | null;
  timestamp: string;
};

export type ApiError = {
  status: number;
  errorCode: string | null;
  message: string;
  requestId?: string;
  validationErrors?: Record<string, string[]>;
};

export class HttpError extends Error {
  status: number;
  errorCode: string | null;
  requestId?: string;
  validationErrors?: Record<string, string[]>;

  constructor(error: ApiError) {
    super(error.message);
    this.name = "HttpError";
    this.status = error.status;
    this.errorCode = error.errorCode;
    this.requestId = error.requestId;
    this.validationErrors = error.validationErrors;
  }
}

/** Result of paginating a list in memory inside the BFF. */
export type PageResult<T> = {
  items: T[];
  totalCount: number;
  page: number;
  size: number;
};

export type SortDir = "asc" | "desc";
