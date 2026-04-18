export interface ResourceAllocationSearchRequest {
  keyword?: string | null;
  userId?: string | null;
  role?: string | null;
  /** Date/ISO string — gửi nguyên từ app-input, backend tự parse */
  month?: string | Date | null;
  pageIndex: number;
  pageSize: number;
  voided?: boolean;
}
