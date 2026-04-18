export type ResourceAllocation = {
  id: string;
  userId: string;
  userDisplay?: string | null;
  role: string;
  month?: string | Date | null;
  startDate?: string | Date | null;
  endDate?: string | Date | null;
  allocationPercent: number;
};

export type ResourceAllocationWritePayload = {
  userId: string;
  role: string;
  month: string | Date;
  startDate?: string | Date | null;
  endDate?: string | Date | null;
  allocationPercent: number;
};
