export type SkillSearchRequest = {
  keyword?: string | null;
  category?: string | null;
  pageIndex?: number;
  pageSize?: number;
  voided?: boolean | null;
  ids?: string[] | null;
};

