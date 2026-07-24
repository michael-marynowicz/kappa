import {
  Component,
  OnInit,
  OnDestroy,
  inject,
  signal,
  computed,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { AuthStateService } from "../../../../core/services/auth-state.service";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";
import { I18nService } from "../../../../i18n/i18n.service";

const RESEND_COOLDOWN_SECONDS = 60;

@Component({
  selector: "app-login",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe],
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
})
export class LoginComponent implements OnInit, OnDestroy {
  readonly authState = inject(AuthStateService);
  private readonly i18n = inject(I18nService);
  email = "";
  password = "";
  showPassword = false;

  readonly resendCooldown = signal(0);
  readonly resendCountdownLabel = computed(() =>
    this.i18n.tWithParams("auth.login.resend_countdown", {
      n: this.resendCooldown().toString(),
    }),
  );
  private cooldownInterval: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.authState.clearError();
  }

  ngOnDestroy(): void {
    this.clearCooldownInterval();
  }

  onSubmit(): void {
    this.authState.login({ email: this.email, password: this.password });
  }

  resendVerification(): void {
    const email = this.authState.emailUnverified();
    if (!email || this.resendCooldown() > 0) return;
    this.authState.resendVerificationEmail(email);
    this.startCooldown();
  }

  private startCooldown(): void {
    this.resendCooldown.set(RESEND_COOLDOWN_SECONDS);
    this.clearCooldownInterval();
    this.cooldownInterval = setInterval(() => {
      const remaining = this.resendCooldown() - 1;
      this.resendCooldown.set(remaining);
      if (remaining <= 0) this.clearCooldownInterval();
    }, 1000);
  }

  private clearCooldownInterval(): void {
    if (this.cooldownInterval !== null) {
      clearInterval(this.cooldownInterval);
      this.cooldownInterval = null;
    }
  }
}
