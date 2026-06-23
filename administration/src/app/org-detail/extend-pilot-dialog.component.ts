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
import { MatButtonModule } from "@angular/material/button";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { CommonModule } from "@angular/common";

export interface ExtendPilotDialogData {
  orgName: string;
  currentExpiry: string | null;
}

export interface ExtendPilotDialogResult {
  pilotExpiresAt: string;
}

@Component({
  selector: "app-extend-pilot-dialog",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
  ],
  templateUrl: "./extend-pilot-dialog.component.html",
})
export class ExtendPilotDialogComponent {
  form: FormGroup;
  readonly minDate: Date;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<
      ExtendPilotDialogComponent,
      ExtendPilotDialogResult | null
    >,
    @Inject(MAT_DIALOG_DATA) public data: ExtendPilotDialogData,
  ) {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    if (data.currentExpiry) {
      const current = new Date(data.currentExpiry);
      current.setDate(current.getDate() + 1);
      this.minDate = current > tomorrow ? current : tomorrow;
    } else {
      this.minDate = tomorrow;
    }

    this.form = this.fb.group({
      pilotExpiresAt: [null, Validators.required],
    });
  }

  formatCurrentExpiry(): string {
    if (!this.data.currentExpiry) return "—";
    return new Date(this.data.currentExpiry).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }

  confirm(): void {
    if (this.form.valid) {
      const date = new Date(this.form.value.pilotExpiresAt);
      date.setHours(23, 59, 59, 0);
      this.dialogRef.close({ pilotExpiresAt: date.toISOString() });
    }
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
