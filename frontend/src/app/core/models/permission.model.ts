/**
 * Permissions are provided by the backend.
 * The frontend NEVER defines business logic around which plan gets which permission.
 * It only consumes the permission list from the user's session.
 */

export type PlanCode = "FREE" | "PREMIUM";

export interface UserPermissions {
  plan: PlanCode;
  permissions: string[];
}

/**
 * Known permission codes (for type-safety in templates).
 * This enum mirrors backend codes — it does NOT define access rules.
 */
export enum Permission {
  // Sprint / US table
  US_TABLE_VIEW = "US_TABLE_VIEW",
  US_EDIT_SP = "US_EDIT_SP",
  SPRINT_TRACKING = "SPRINT_TRACKING",

  // Metrics
  METRICS_BASIC = "METRICS_BASIC",
  METRICS_VELOCITY = "METRICS_VELOCITY",
  METRICS_COMPARISON = "METRICS_COMPARISON",

  // Capacity
  CAPACITY_VIEW = "CAPACITY_VIEW",
  CAPACITY_MANAGE = "CAPACITY_MANAGE",

  // Export
  EXPORT_CSV = "EXPORT_CSV",

  // Backlog
  BACKLOG_VIEW = "BACKLOG_VIEW",
  BACKLOG_MANAGE = "BACKLOG_MANAGE",
}

/**
 * State of a feature gate:
 * - enabled: user has permission, feature is fully functional
 * - disabled: user does NOT have permission, UI is greyed with premium overlay
 * - hidden: feature is completely removed from the DOM
 */
export type FeatureGateState = "enabled" | "disabled" | "hidden";
