import { Pipe, PipeTransform, inject } from "@angular/core";
import { PermissionService } from "../../core/services/permission.service";

/**
 * Pipe to check permission in templates.
 * Usage: {{ 'EXPORT_CSV' | hasPermission }}
 * Returns boolean.
 */
@Pipe({
  name: "hasPermission",
  standalone: true,
  pure: false,
})
export class HasPermissionPipe implements PipeTransform {
  private readonly permissionService = inject(PermissionService);

  transform(permission: string): boolean {
    return this.permissionService.hasPermission(permission);
  }
}
