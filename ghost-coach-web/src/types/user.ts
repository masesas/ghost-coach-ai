import type { ExperienceLevel, Sport } from "@/lib/constants";

export interface User {
  id: number;
  email: string;
  fullName: string;
  sport: Sport;
  position: string;
  experienceLevel: ExperienceLevel;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RegisterPayload {
  email: string;
  password: string;
  fullName: string;
  sport: Sport;
  position: string;
  experienceLevel: ExperienceLevel;
}

export interface LoginPayload {
  email: string;
  password: string;
}
