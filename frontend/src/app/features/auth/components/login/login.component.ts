import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { AuthStateService } from "../../../../core/services/auth-state.service";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
})
export class LoginComponent {
  readonly authState = inject(AuthStateService);
  email = "";
  password = "";
  showPassword = false;

  onSubmit(): void {
    this.authState.login({ email: this.email, password: this.password });
  }
}
