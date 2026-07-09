import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { SprintIssueTableComponent } from "./sprint-issue-table.component";
import { SprintIssue } from "../../models/sprint-issue.model";

describe("SprintIssueTableComponent", () => {
  let component: SprintIssueTableComponent;
  let fixture: ComponentFixture<SprintIssueTableComponent>;

  const mockIssues: SprintIssue[] = [
    {
      issueKey: "SCRUM-1",
      summary: "Login feature",
      status: "In Progress",
      assignee: "Alice",
      issueType: "Story",
      totalStoryPoints: 8,
      remainingStoryPoints: 3,
      doneStoryPoints: 5,
    },
    {
      issueKey: "SCRUM-2",
      summary: "Fix bug",
      status: "Done",
      assignee: null,
      issueType: "Bug",
      totalStoryPoints: 3,
      remainingStoryPoints: 0,
      doneStoryPoints: 3,
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SprintIssueTableComponent, FormsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(SprintIssueTableComponent);
    component = fixture.componentInstance;
    component.issues = mockIssues;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should render the correct number of rows", () => {
    const rows = fixture.nativeElement.querySelectorAll(".issue-row");
    expect(rows.length).toBe(2);
  });

  it("should display issue count", () => {
    const count = fixture.nativeElement.querySelector(".table-count");
    expect(count.textContent).toContain("2 sprint issues");
  });

  // -----------------------------------------------------------------------
  describe("empty state", () => {
    it("should show empty state when no issues", () => {
      component.issues = [];
      fixture.detectChanges();
      const empty = fixture.nativeElement.querySelector(".empty-state");
      expect(empty).toBeTruthy();
    });
  });

  // -----------------------------------------------------------------------
  describe("inline editing", () => {
    it("should enter edit mode on startEdit", () => {
      component.startEdit(mockIssues[0]);
      expect(component.isEditing("SCRUM-1")).toBeTrue();
      expect(component.editingState()?.value).toBe(3); // remainingStoryPoints
    });

    it("should exit edit mode on cancelEdit", () => {
      component.startEdit(mockIssues[0]);
      component.cancelEdit();
      expect(component.isEditing("SCRUM-1")).toBeFalse();
      expect(component.editingState()).toBeNull();
    });

    it("should emit updateRemainingSp on confirmEdit", () => {
      spyOn(component.updateRemainingSp, "emit");
      component.startEdit(mockIssues[0]);
      component.editingState()!.value = 1;

      component.confirmEdit(mockIssues[0]);

      expect(component.updateRemainingSp.emit).toHaveBeenCalledWith({
        issueKey: "SCRUM-1",
        remainingStoryPoints: 1,
      });
      expect(component.editingState()).toBeNull();
    });

    it("should clamp negative values to 0", () => {
      spyOn(component.updateRemainingSp, "emit");
      component.startEdit(mockIssues[0]);
      component.editingState()!.value = -5;

      component.confirmEdit(mockIssues[0]);

      expect(component.updateRemainingSp.emit).toHaveBeenCalledWith({
        issueKey: "SCRUM-1",
        remainingStoryPoints: 0,
      });
    });

    it("should floor decimal values", () => {
      spyOn(component.updateRemainingSp, "emit");
      component.startEdit(mockIssues[0]);
      component.editingState()!.value = 2.7;

      component.confirmEdit(mockIssues[0]);

      expect(component.updateRemainingSp.emit).toHaveBeenCalledWith({
        issueKey: "SCRUM-1",
        remainingStoryPoints: 2,
      });
    });

    it("should not emit when editingState is null", () => {
      spyOn(component.updateRemainingSp, "emit");
      component.confirmEdit(mockIssues[0]);
      expect(component.updateRemainingSp.emit).not.toHaveBeenCalled();
    });

    it("should default to totalStoryPoints when remainingStoryPoints is null", () => {
      const issueNoRemaining: SprintIssue = {
        ...mockIssues[0],
        remainingStoryPoints: null,
        totalStoryPoints: 8,
      };
      component.startEdit(issueNoRemaining);
      expect(component.editingState()?.value).toBe(8);
    });
  });

  // -----------------------------------------------------------------------
  describe("isEditing()", () => {
    it("should return false when not editing", () => {
      expect(component.isEditing("SCRUM-1")).toBeFalse();
    });

    it("should return true only for the editing issue", () => {
      component.startEdit(mockIssues[0]);
      expect(component.isEditing("SCRUM-1")).toBeTrue();
      expect(component.isEditing("SCRUM-2")).toBeFalse();
    });
  });

  // -----------------------------------------------------------------------
  describe("rendering", () => {
    it('should show "Unassigned" for null assignee', () => {
      const assignees = fixture.nativeElement.querySelectorAll(".assignee");
      expect(assignees[1].textContent.trim()).toBe("Unassigned");
    });

    it("should display issue keys", () => {
      const keys = fixture.nativeElement.querySelectorAll(".issue-key");
      expect(keys[0].textContent.trim()).toBe("SCRUM-1");
      expect(keys[1].textContent.trim()).toBe("SCRUM-2");
    });

    it("should show saving indicator when savingIssueKey matches", () => {
      component.savingIssueKey = "SCRUM-1";
      fixture.detectChanges();
      const saving = fixture.nativeElement.querySelector(".saving-indicator");
      expect(saving).toBeTruthy();
      expect(saving.textContent).toContain("Saving");
    });
  });
});
