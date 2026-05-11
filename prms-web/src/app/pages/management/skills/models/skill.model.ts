export type Skill = {
  id: string;
  name: string;
  category?: string | null;
  description?: string | null;
  code: string;
};

export type SkillWritePayload = {
  name: string;
  category?: string | null;
  description?: string | null;
  code: string;
};

