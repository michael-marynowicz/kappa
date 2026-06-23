import { Component, Inject } from "@angular/core";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatButtonModule } from "@angular/material/button";
import { CommonModule } from "@angular/common";

export interface InviteUsersDialogData {
  orgName: string;
}

export interface InviteUsersDialogResult {
  emails: string[];
  role: string;
}

@Component({
  selector: "app-invite-users-dialog",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: "./invite-users-dialog.component.html",
})
export class InviteUsersDialogComponent {
  form: FormGroup;
  readonly roleOptions = ["MEMBER", "ADMIN"];
  parsedEmails: string[] = [];
  parseError = "";

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<
      InviteUsersDialogComponent,
      InviteUsersDialogResult | null
    >,
    @Inject(MAT_DIALOG_DATA) public data: InviteUsersDialogData,
  ) {
    this.form = this.fb.group({
      emailsRaw: ["", Validators.required],
      role: ["MEMBER", Validators.required],
    });
  }

  parseEmails(): void {
    const raw: string = this.form.value.emailsRaw ?? "";
    const candidates = raw
      .split(/[\s,;]+/)
      .map((e: string) => e.trim())
      .filter((e: string) => e.length > 0);
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const invalid = candidates.filter((e: string) => !emailRegex.test(e));
    if (invalid.length > 0) {
      this.parseError = `Invalid email(s): ${invalid.join(", ")}`;
      this.parsedEmails = [];
    } else {
      this.parseError = "";
      this.parsedEmails = [...new Set(candidates)];
    }
  }

  confirm(): void {
    this.parseEmails();
    if (this.parsedEmails.length > 0 && !this.parseError && this.form.valid) {
      this.dialogRef.close({
        emails: this.parsedEmails,
        role: this.form.value.role,
      });
    }
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
