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

/** Cập nhật hồ sơ của user đăng nhập (API /users/current/profile) — không gồm username */
export type CurrentUserProfilePayload = {
  email: string;
  fullName: string;
};

