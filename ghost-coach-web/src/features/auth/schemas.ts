import { z } from "zod";
import { EXPERIENCE_LEVELS, SPORTS } from "@/lib/constants";

export const loginSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(1, "Password is required"),
});

export const registerSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(8, "Min 8 characters"),
  fullName: z.string().min(2, "Required"),
  sport: z.enum(SPORTS),
  position: z.string().min(1, "Required"),
  experienceLevel: z.enum(EXPERIENCE_LEVELS),
});

export type LoginValues = z.infer<typeof loginSchema>;
export type RegisterValues = z.infer<typeof registerSchema>;
