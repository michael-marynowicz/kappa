import { ComponentFixture, TestBed } from "@angular/core/testing";
import { signal, NO_ERRORS_SCHEMA } from "@angular/core";
import { SprintDashboardComponent } from "./sprint-dashboard.component";
import { SprintStateService } from "../../services/sprint-state.service";
import { SprintSummary } from "../../models/sprint-issue.model";

describe("SprintDashboardComponent", () => {
  let component: SprintDashboardComponent;
  let fixture: ComponentFixture<SprintDashboardComponent>;
  let stateMock: any;

  const mockSummary: SprintSummary = {
    totalIssues: 5,
    totalStoryPoints: 20,
    remainingStoryPoints: 5,
    doneStoryPoints: 15,
    completionPercentage: 75,
  };

  beforeEach(async () => {
    stateMock = {
      issues: signal([]),
      metrics: signal(null),
      iterations: signal([]),
      loading: signal(false),
      metricsLoading: signal(false),
      error: signal(null),
      issuesError: signal(null),
      metricsError: signal(null),
      iterationsError: signal(null),
      savingIssueKey: signal(null),
      metricsGated: signal(false),
      iterationsGated: signal(false),
      exportGated: signal(false),
      summary: signal(mockSummary),
      loadIssues: jasmine.createSpy("loadIssues"),
      loadMetrics: jasmine.createSpy("loadMetrics"),
      loadIterations: jasmine.createSpy("loadIterations"),
      updateRemainingStoryPoints: jasmine.createSpy(
        "updateRemainingStoryPoints",
      ),
      exportCsv: jasmine.createSpy("exportCsv"),
      clearError: jasmine.createSpy("clearError"),
    };

    await TestBed.configureTestingModule({
      imports: [SprintDashboardComponent],
    })
      .overrideComponent(SprintDashboardComponent, {
        remove: {
          imports: [
            // Remove real child components
          ],
        },
        add: {
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .overrideProvider(SprintStateService, { useValue: stateMock })
      .compileComponents();

    fixture = TestBed.createComponent(SprintDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should call all load methods on init", () => {
    expect(stateMock.loadIssues).toHaveBeenCalled();
    expect(stateMock.loadMetrics).toHaveBeenCalled();
    expect(stateMock.loadIterations).toHaveBeenCalled();
  });

  it("should default to board tab", () => {
    expect(component.activeTab).toBe("board");
  });

  // -----------------------------------------------------------------------
  describe("tab switching", () => {
    it("should switch to metrics tab", () => {
      component.activeTab = "metrics";
      expect(component.activeTab).toBe("metrics");
    });

    it("should switch to capacity tab", () => {
      component.activeTab = "capacity";
      expect(component.activeTab).toBe("capacity");
    });

    it("should switch back to board tab", () => {
      component.activeTab = "metrics";
      component.activeTab = "board";
      expect(component.activeTab).toBe("board");
    });
  });

  // -----------------------------------------------------------------------
  describe("onUpdateRemainingStoryPoints()", () => {
    it("should delegate to state service", () => {
      component.onUpdateRemainingStoryPoints({
        issueKey: "SCRUM-1",
        remainingStoryPoints: 2,
      });
      expect(stateMock.updateRemainingStoryPoints).toHaveBeenCalledWith(
        "SCRUM-1",
        2,
      );
    });
  });

  // -----------------------------------------------------------------------
  describe("onExportCsv()", () => {
    it("should delegate to state service", () => {
      component.onExportCsv();
      expect(stateMock.exportCsv).toHaveBeenCalled();
    });
  });
});
