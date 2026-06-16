import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  Subscription,
  Plan,
  SubscribeRequest,
  RedeemRequest,
  FeatureAccess,
  CheckoutSession,
} from "../models/subscription.model";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class SubscriptionApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/subscription`;

  getCurrent(): Observable<Subscription> {
    return this.http.get<Subscription>(this.baseUrl);
  }

  changePlan(request: SubscribeRequest): Observable<Subscription> {
    return this.http.put<Subscription>(`${this.baseUrl}/plan`, request);
  }

  checkFeatureAccess(featureCode: string): Observable<FeatureAccess> {
    return this.http.get<FeatureAccess>(
      `${this.baseUrl}/features/${encodeURIComponent(featureCode)}`,
    );
  }

  cancel(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/cancel`, {});
  }

  createCheckoutSession(planCode: string): Observable<CheckoutSession> {
    return this.http.post<CheckoutSession>(`${this.baseUrl}/checkout`, {
      planCode,
    });
  }

  getPlans(): Observable<Plan[]> {
    return this.http.get<Plan[]>(`${environment.apiBaseUrl}/api/v1/plans`);
  }

  redeem(request: RedeemRequest): Observable<Subscription> {
    return this.http.post<Subscription>(
      `${environment.apiBaseUrl}/api/v1/promo/redeem`,
      request,
    );
  }
}
