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
import { MatSelectModule } from "@angular/material/select";
import { MatButtonModule } from "@angular/material/button";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatInputModule } from "@angular/material/input";

export interface AssignPilotDialogData {
  orgName: string;
}

export interface AssignPilotDialogResult {
  planCode: string;
  pilotExpiresAt: string;
}

@Component({
  selector: "app-assign-pilot-dialog",
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatDatepickerModule,
    MatInputModule,
  ],
  templateUrl: "./assign-pilot-dialog.component.html",
})
export class AssignPilotDialogComponent {
  form: FormGroup;
  readonly planOptions = ["FREE", "PRO", "BUSINESS"];
  readonly minDate = new Date();

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<
      AssignPilotDialogComponent,
      AssignPilotDialogResult | null
    >,
    @Inject(MAT_DIALOG_DATA) public data: AssignPilotDialogData,
  ) {
    this.form = this.fb.group({
      planCode: ["BUSINESS", Validators.required],
      pilotExpiresAt: [null, Validators.required],
    });
  }

  confirm(): void {
    if (this.form.valid) {
      const date: Date = new Date(this.form.value.pilotExpiresAt);
      date.setHours(23, 59, 59, 0);
      this.dialogRef.close({
        planCode: this.form.value.planCode,
        pilotExpiresAt: date.toISOString(),
      });
    }
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
