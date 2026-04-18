export type AppParam = {
  id: string;
  description?: string | null;
  paramGroup: string;
  paramName: string;
  paramValue?: string | null;
  paramType?: string | null;
};

export type AppParamWritePayload = {
  description?: string | null;
  paramGroup: string;
  paramName: string;
  paramValue?: string | null;
  paramType?: string | null;
};

