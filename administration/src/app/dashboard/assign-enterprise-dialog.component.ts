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

export interface AssignEnterpriseDialogData {
  orgName: string;
}

export interface AssignEnterpriseDialogResult {
  planCode: string;
}

@Component({
  selector: "app-assign-enterprise-dialog",
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: "./assign-enterprise-dialog.component.html",
})
export class AssignEnterpriseDialogComponent {
  form: FormGroup;
  readonly planOptions = ["FREE", "PRO", "BUSINESS"];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<
      AssignEnterpriseDialogComponent,
      AssignEnterpriseDialogResult | null
    >,
    @Inject(MAT_DIALOG_DATA) public data: AssignEnterpriseDialogData,
  ) {
    this.form = this.fb.group({
      planCode: ["BUSINESS", Validators.required],
    });
  }

  confirm(): void {
    if (this.form.valid) {
      this.dialogRef.close(this.form.value as AssignEnterpriseDialogResult);
    }
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
