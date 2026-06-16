export interface Subscription {
  id: string;
  planCode: string;
  planName: string;
  status: SubscriptionStatus;
  currentPeriodEnd: string | null;
  cancelAtPeriodEnd: boolean;
}

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
  priceMonthly: number;
  priceYearly: number;
  trialDays: number;
  features: string[];
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
