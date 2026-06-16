import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { SubscriptionStateService } from "../../../../core/services/subscription-state.service";
import { HumanizePipe } from "../../../../shared/pipes/humanize.pipe";

@Component({
  selector: "app-billing",
  standalone: true,
  imports: [CommonModule, FormsModule, HumanizePipe],
  templateUrl: "./billing.component.html",
  styleUrls: ["./billing.component.scss"],
})
export class BillingComponent implements OnInit {
  readonly subState = inject(SubscriptionStateService);
  promoCode = "";
  showCancelModal = false;

  ngOnInit(): void {
    this.subState.loadSubscription();
    this.subState.loadPlans();
  }

  onSelectPlan(planCode: string): void {
    this.subState.startCheckout(planCode);
  }

  onCancel(): void {
    this.showCancelModal = true;
  }

  confirmCancel(): void {
    this.showCancelModal = false;
    this.subState.cancel();
  }

  onRedeem(): void {
    const code = this.promoCode.trim();
    if (!code) return;
    this.subState.redeem(code);
    this.promoCode = "";
  }
}
