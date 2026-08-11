/**
 * Capacity calculation utilities
 * All rules per specifications:
 * - Effective days = max(0, Days - Days off sprint) × Time override
 * - Total sprint = somme des Effective days
 * - 80% = Total × 0.8
 * - SP = arrondi(80% / 1.5)
 */

/**
 * Calculate effective days for a member/sprint combination
 * Effective days = max(0, Days - Days off sprint) × Time override
 */
export function calculateEffectiveDays(
  days: number,
  daysOff: number,
  timeOverride: number,
): number {
  const availableDays = Math.max(0, days - daysOff);
  return availableDays * timeOverride;
}

/**
 * Calculate total sprint capacity (sum of all effective days)
 */
export function calculateSprintTotal(
  memberIds: string[],
  sprints: string[],
  sprintToConsider: string,
  getDaysForSprint: (sprint: string) => number,
  getDaysOff: (memberId: string, sprint: string) => number,
  getTimeOverride: (memberId: string) => number,
): number {
  let total = 0;
  for (const memberId of memberIds) {
    const days = getDaysForSprint(sprintToConsider);
    const daysOff = getDaysOff(memberId, sprintToConsider);
    const timeOverride = getTimeOverride(memberId);
    total += calculateEffectiveDays(days, daysOff, timeOverride);
  }
  return total;
}

/**
 * Calculate 80% of sprint total
 */
export function calculate80Percent(total: number): number {
  return total * 0.8;
}

/**
 * Calculate story points from 80% (rounded)
 * SP = arrondi(80% / 1.5)
 */
export function calculateSP(percent80: number): number {
  return Math.round(percent80 / 1.5);
}

/**
 * Validate days off: must be >= 0
 */
export function isValidDaysOff(daysOff: number): boolean {
  return daysOff >= 0;
}

/**
 * Validate time override: must be between 0 and 1
 */
export function isValidTimeOverride(timeOverride: number): boolean {
  return timeOverride >= 0 && timeOverride <= 1;
}

/**
 * Clamp days off to [0, max]
 */
export function clampDaysOff(daysOff: number, max: number): number {
  return Math.max(0, Math.min(daysOff, max));
}

/**
 * Clamp time override to [0, 1]
 */
export function clampTimeOverride(timeOverride: number): number {
  return Math.max(0, Math.min(timeOverride, 1));
}
