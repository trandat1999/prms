export type UserSearchRequest = {
  keyword?: string | null;
  pageIndex?: number | null;
  pageSize?: number | null;
  voided?: boolean | null;
  ids?: string[] | null;
  enabled?: boolean | null;
};

