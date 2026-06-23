import { Component } from "@angular/core";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { Router } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";

import { AuthService } from "../services/auth.service";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: "./login.component.html",
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  errorMessage = "";

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      email: ["", [Validators.required, Validators.email]],
      password: ["", Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading = true;
    this.errorMessage = "";

    const { email, password } = this.form.value;
    this.authService.login(email, password).subscribe({
      next: () => this.router.navigate(["/dashboard"]),
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err.status === 401
            ? "Invalid credentials. Please try again."
            : "Login failed. Please check your connection and try again.";
      },
    });
  }
}
