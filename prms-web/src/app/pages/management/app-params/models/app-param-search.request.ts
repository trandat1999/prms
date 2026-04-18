export type AppParamSearchRequest = {
  keyword?: string | null;
  pageIndex?: number | null;
  pageSize?: number | null;
  voided?: boolean | null;
  ids?: string[] | null;
  paramGroup?: string | null;
  paramType?: string | null;
};

