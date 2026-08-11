export interface Subscription {
  id: string;
  planCode: string;
  planName: string;
  status: SubscriptionStatus;
  subscriptionType: SubscriptionType;
  currentPeriodEnd: string | null;
  pilotExpiresAt: string | null;
  cancelAtPeriodEnd: boolean;
  /** Backend-computed convenience flags */
  showPaymentPages: boolean;
  isEnterprise: boolean;
  isPilot: boolean;
}

export type SubscriptionType = "SELF_SERVICE" | "ENTERPRISE" | "PILOT";

export type SubscriptionStatus =
  | "ACTIVE"
  | "TRIALING"
  | "PAST_DUE"
  | "CANCELED"
  | "NONE";

export interface Plan {
  id: string;
  code: string;
  name: string;
  description: string;
  maxUsers: number | null;
  maxDashboards: number | null;
  priceMonthly: number;
  priceYearly: number;
  trialDays: number;
  features: string[];
  contactOnly?: boolean;
}

export interface SubscribeRequest {
  planCode: string;
}

export interface RedeemRequest {
  promoCode: string;
}

export interface FeatureAccess {
  featureCode: string;
  hasAccess: boolean;
}

export interface CheckoutSession {
  checkoutUrl: string;
  sessionId: string;
}
