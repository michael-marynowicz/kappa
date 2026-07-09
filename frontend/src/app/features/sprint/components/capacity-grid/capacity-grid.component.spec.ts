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
      { id: "m1", name: "Alice", role: "DEV" },
      { id: "m2", name: "Bob", role: "QA" },
    ],
    sprints: ["Sprint 1", "Sprint 2"],
    daysPerSprint: { "Sprint 1": 10, "Sprint 2": 9 },
    daysOffGrid: {
      m1: { "Sprint 1": 2, "Sprint 2": 0 },
      m2: { "Sprint 1": 1, "Sprint 2": 3 },
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

  // -----------------------------------------------------------------------
  describe("getDaysOff()", () => {
    it("should return days off from grid", () => {
      expect(component.getDaysOff("m1", "Sprint 1")).toBe(2);
      expect(component.getDaysOff("m2", "Sprint 2")).toBe(3);
    });

    it("should return 0 for missing entries", () => {
      expect(component.getDaysOff("m1", "Sprint 99")).toBe(0);
      expect(component.getDaysOff("unknown", "Sprint 1")).toBe(0);
    });
  });

  // -----------------------------------------------------------------------
  describe("getDaysForSprint()", () => {
    it("should return working days for a sprint", () => {
      expect(component.getDaysForSprint("Sprint 1")).toBe(10);
      expect(component.getDaysForSprint("Sprint 2")).toBe(9);
    });

    it("should return 0 for unknown sprint", () => {
      expect(component.getDaysForSprint("Unknown")).toBe(0);
    });
  });

  // -----------------------------------------------------------------------
  describe("getTotalAvailable()", () => {
    it("should compute total available days across sprints", () => {
      // Alice: (10-2) + (9-0) = 8 + 9 = 17
      expect(component.getTotalAvailable("m1")).toBe(17);
      // Bob: (10-1) + (9-3) = 9 + 6 = 15
      expect(component.getTotalAvailable("m2")).toBe(15);
    });
  });

  // -----------------------------------------------------------------------
  describe("getAvailByRole()", () => {
    it("should sum availability for DEV role in a sprint", () => {
      // Only Alice is DEV: 10-2 = 8
      expect(component.getAvailByRole("DEV", "Sprint 1")).toBe(8);
    });

    it("should sum availability for QA role in a sprint", () => {
      // Only Bob is QA: 10-1 = 9
      expect(component.getAvailByRole("QA", "Sprint 1")).toBe(9);
    });

    it("should return 0 for role with no members", () => {
      expect(component.getAvailByRole("PDA", "Sprint 1")).toBe(0);
    });
  });

  // -----------------------------------------------------------------------
  describe("getTotalByRole()", () => {
    it("should sum total availability across all sprints for a role", () => {
      // Alice DEV: (10-2) + (9-0) = 17
      expect(component.getTotalByRole("DEV")).toBe(17);
    });
  });

  // -----------------------------------------------------------------------
  describe("getSprintTotal()", () => {
    it("should sum total availability for all members in a sprint", () => {
      // Sprint 1: Alice(10-2) + Bob(10-1) = 8 + 9 = 17
      expect(component.getSprintTotal("Sprint 1")).toBe(17);
    });
  });

  // -----------------------------------------------------------------------
  describe("getGrandTotal()", () => {
    it("should sum total availability across all sprints and members", () => {
      // Sprint 1: 8+9=17, Sprint 2: 9+6=15 → 32
      expect(component.getGrandTotal()).toBe(32);
    });
  });

  // -----------------------------------------------------------------------
  describe("onDaysOffChange()", () => {
    it("should clamp value and call state updateDaysOff", () => {
      component.onDaysOffChange("m1", "Sprint 1", 5);
      expect(stateMock.updateDaysOff).toHaveBeenCalledWith("m1", "Sprint 1", 5);
    });

    it("should clamp negative values to 0", () => {
      component.onDaysOffChange("m1", "Sprint 1", -3);
      expect(stateMock.updateDaysOff).toHaveBeenCalledWith("m1", "Sprint 1", 0);
    });

    it("should clamp values exceeding max to daysPerSprint", () => {
      component.onDaysOffChange("m1", "Sprint 1", 15);
      expect(stateMock.updateDaysOff).toHaveBeenCalledWith(
        "m1",
        "Sprint 1",
        10,
      );
    });
  });

  // -----------------------------------------------------------------------
  describe("onAddMember()", () => {
    it("should call addMember and reset name", () => {
      component.newMemberName = "  Charlie  ";
      component.newMemberRole = "PDA";

      component.onAddMember();

      expect(stateMock.addMember).toHaveBeenCalledWith("Charlie", "PDA");
      expect(component.newMemberName).toBe("");
    });

    it("should not call addMember for empty name", () => {
      component.newMemberName = "   ";
      component.onAddMember();
      expect(stateMock.addMember).not.toHaveBeenCalled();
    });
  });

  // -----------------------------------------------------------------------
  describe("startEdit / cancelEdit / saveEdit", () => {
    const member = { id: "m1", name: "Alice", role: "DEV" as const };

    it("should set editing state on startEdit", () => {
      component.startEdit(member);
      expect(component.editingMemberId).toBe("m1");
      expect(component.editName).toBe("Alice");
      expect(component.editRole).toBe("DEV");
    });

    it("should clear editing state on cancelEdit", () => {
      component.startEdit(member);
      component.cancelEdit();
      expect(component.editingMemberId).toBeNull();
    });

    it("should call updateMember and clear editing on saveEdit", () => {
      component.startEdit(member);
      component.editName = "Alice Updated";
      component.editRole = "PDA";

      component.saveEdit(member);

      expect(stateMock.updateMember).toHaveBeenCalledWith(
        member,
        "Alice Updated",
        "PDA",
      );
      expect(component.editingMemberId).toBeNull();
    });

    it("should not save with empty name", () => {
      component.startEdit(member);
      component.editName = "   ";
      component.saveEdit(member);
      expect(stateMock.updateMember).not.toHaveBeenCalled();
    });
  });

  // -----------------------------------------------------------------------
  describe("onDelete()", () => {
    const member = { id: "m1", name: "Alice", role: "DEV" as const };

    it("should call deleteMember when user confirms", () => {
      spyOn(window, "confirm").and.returnValue(true);
      component.onDelete(member);
      expect(stateMock.deleteMember).toHaveBeenCalledWith("m1");
    });

    it("should not call deleteMember when user cancels", () => {
      spyOn(window, "confirm").and.returnValue(false);
      component.onDelete(member);
      expect(stateMock.deleteMember).not.toHaveBeenCalled();
    });
  });

  // -----------------------------------------------------------------------
  describe("trackMember()", () => {
    it("should return member id", () => {
      expect(
        component.trackMember(0, { id: "m1", name: "Alice", role: "DEV" }),
      ).toBe("m1");
    });
  });
});
