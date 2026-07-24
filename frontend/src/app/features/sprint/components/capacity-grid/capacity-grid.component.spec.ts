import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { signal } from "@angular/core";
import { CapacityGridComponent } from "./capacity-grid.component";
import { CapacityStateService } from "../../services/capacity-state.service";
import { CapacityGrid } from "../../models/capacity.model";

describe("CapacityGridComponent", () => {
  let component: CapacityGridComponent;
  let fixture: ComponentFixture<CapacityGridComponent>;
  let stateMock: jasmine.SpyObj<CapacityStateService>;

  const mockGrid: CapacityGrid = {
    members: [
      { id: "m1", name: "Alice", role: "DEV", timeOverride: 1.0 },
      { id: "m2", name: "Bob", role: "QA", timeOverride: 0.5 },
    ],
    sprints: ["Sprint 1", "Sprint 2"],
    daysPerSprint: { "Sprint 1": 10, "Sprint 2": 9 },
    daysOffGrid: {
      m1: { "Sprint 1": 2, "Sprint 2": 0 },
      m2: { "Sprint 1": 1, "Sprint 2": 3 },
    },
    sprintDetails: {
      "Sprint 1": { pi: "1.0", iteration: 1, ip: false },
      "Sprint 2": { pi: "1.0", iteration: 2, ip: false },
    },
  };

  beforeEach(async () => {
    stateMock = jasmine.createSpyObj(
      "CapacityStateService",
      [
        "loadGrid",
        "addMember",
        "updateMember",
        "deleteMember",
        "updateDaysOff",
        "clearError",
      ],
      {
        grid: signal<CapacityGrid | null>(mockGrid),
        loading: signal(false),
        error: signal<string | null>(null),
        savingCell: signal<string | null>(null),
        gated: signal(false),
      },
    );

    await TestBed.configureTestingModule({
      imports: [CapacityGridComponent, FormsModule],
    })
      .overrideComponent(CapacityGridComponent, {
        set: {
          providers: [],
        },
      })
      .overrideProvider(CapacityStateService, { useValue: stateMock })
      .compileComponents();

    fixture = TestBed.createComponent(CapacityGridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should call loadGrid on init", () => {
    expect(stateMock.loadGrid).toHaveBeenCalled();
  });

  it("should handle member addition", () => {
    component.newMemberName = "Charlie";
    component.newMemberRole = "DEV";
    component.onAddMember();
    expect(stateMock.addMember).toHaveBeenCalledWith("Charlie", "DEV");
  });

  it("should sanitize member names", () => {
    component.newMemberName = "  John  <script>alert('xss')</script>  ";
    component.newMemberRole = "QA";
    component.onAddMember();
    expect(stateMock.addMember).toHaveBeenCalledWith("John", "QA");
  });

  it("should handle member edit", () => {
    const member = {
      id: "m1",
      name: "Alice",
      role: "DEV" as const,
      timeOverride: 1.0,
    };
    component.startEdit(member);
    expect(component.editingMemberId).toBe("m1");

    component.editName = "Alice Updated";
    component.editRole = "PDA";
    component.saveEdit(member);
    expect(stateMock.updateMember).toHaveBeenCalled();
  });

  it("should handle member deletion with confirmation", () => {
    const member = {
      id: "m1",
      name: "Alice",
      role: "DEV" as const,
      timeOverride: 1.0,
    };
    spyOn(window, "confirm").and.returnValue(true);
    component.onDelete(member);
    expect(stateMock.deleteMember).toHaveBeenCalledWith("m1");
  });
});
