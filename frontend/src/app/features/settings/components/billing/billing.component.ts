import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { SubscriptionStateService } from "../../../../core/services/subscription-state.service";
import { HumanizePipe } from "../../../../shared/pipes/humanize.pipe";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";
import { I18nService } from "../../../../i18n/i18n.service";
import { Plan } from "../../../../core/models/subscription.model";

const ENTERPRISE_CONTACT_EMAIL = "contact@sprint-reporter.io";

@Component({
  selector: "app-billing",
  standalone: true,
  imports: [CommonModule, FormsModule, HumanizePipe, TranslatePipe],
  templateUrl: "./billing.component.html",
  styleUrls: ["./billing.component.scss"],
})
export class BillingComponent implements OnInit {
  readonly subState = inject(SubscriptionStateService);
  private readonly i18n = inject(I18nService);
  promoCode = "";
  showCancelModal = false;

  formatLimit(value: number | null): string {
    return value === null ? this.i18n.t("common.unlimited") : String(value);
  }

  isCurrentPlan(planCode: string): boolean {
    const current = this.subState.subscription()?.planCode;
    if (!current) return false;
    return current.toLowerCase() === planCode.toLowerCase();
  }

  ngOnInit(): void {
    if (this.subState.showPaymentPages()) {
      if (!this.subState.subscription()) this.subState.loadSubscription();
      this.subState.loadPlans();
    }
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

  contactEnterprise(plan: Plan): void {
    const subject = encodeURIComponent(
      `Enterprise Plan — Quote Request (${plan.name})`,
    );
    const body = encodeURIComponent(
      [
        "Hello Sprint Reporter Team,",
        "",
        "I'm interested in the Enterprise plan and would like to get a custom quote.",
        "",
        "— Company Information —",
        "Company Name: ",
        "Industry: ",
        "Website: ",
        "",
        "— Usage Expectations —",
        "Expected Number of Users: ",
        "Number of Jira Projects: ",
        "Sprint Cadence (e.g. 2 weeks): ",
        "",
        "— Contact Details —",
        "Contact Name: ",
        "Contact Email: ",
        "Phone (optional): ",
        "",
        "— Additional Information —",
        "Use Case / Description:",
        "[Please describe how your team plans to use Sprint Reporter]",
        "",
        "Specific requirements or questions (SSO, custom integrations, SLA...):",
        "",
      ].join("\n"),
    );
    window.location.href = `mailto:${ENTERPRISE_CONTACT_EMAIL}?subject=${subject}&body=${body}`;
  }
}
