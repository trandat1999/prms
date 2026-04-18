export type User = {
  id: string;
  username: string;
  email: string;
  fullName: string;
  enabled?: boolean;
  roles?: string[];
};

export type UserDetail = User & {
  accountNonExpired?: boolean;
  accountNonLocked?: boolean;
  credentialsNonExpired?: boolean;
  lastLogin?: string | Date | null;
};

export type UserCreatePayload = {
  username: string;
  password: string;
  email: string;
  fullName: string;
  roleCodes?: string[] | null;
  enabled?: boolean | null;
};

export type UserUpdatePayload = {
  username: string;
  email: string;
  fullName: string;
  roleCodes?: string[] | null;
  enabled?: boolean | null;
};

