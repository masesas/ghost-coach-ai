export const SPORTS = [
  "CRICKET",
  "FOOTBALL",
  "BASKETBALL",
  "BADMINTON",
] as const;
export type Sport = (typeof SPORTS)[number];

export const EXPERIENCE_LEVELS = [
  "BEGINNER",
  "INTERMEDIATE",
  "ADVANCED",
] as const;
export type ExperienceLevel = (typeof EXPERIENCE_LEVELS)[number];

export const SPORT_LABELS: Record<Sport, string> = {
  CRICKET: "Cricket",
  FOOTBALL: "Football",
  BASKETBALL: "Basketball",
  BADMINTON: "Badminton",
};

export const LEVEL_LABELS: Record<ExperienceLevel, string> = {
  BEGINNER: "Beginner",
  INTERMEDIATE: "Intermediate",
  ADVANCED: "Advanced",
};

export const MAX_UPLOAD_SIZE = 5 * 1024 * 1024;
export const ACCEPTED_IMAGE_TYPES = ["image/jpeg", "image/png"];
