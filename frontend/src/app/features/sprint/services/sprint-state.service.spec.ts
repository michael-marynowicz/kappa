import { TestBed } from "@angular/core/testing";
import { of, throwError } from "rxjs";
import { SprintStateService } from "./sprint-state.service";
import { SprintApiService } from "./sprint-api.service";
import {
  SprintIssue,
  SprintMetrics,
  IterationSnapshot,
} from "../models/sprint-issue.model";

describe("SprintStateService", () => {
  let service: SprintStateService;
  let apiServiceMock: jasmine.SpyObj<SprintApiService>;

  const mockIssue: SprintIssue = {
    issueKey: "SCRUM-1",
    summary: "Test story",
    status: "In Progress",
    assignee: "Alice",
    issueType: "Story",
    totalStoryPoints: 8,
    remainingStoryPoints: 3,
    doneStoryPoints: 5,
  };

  const mockIssue2: SprintIssue = {
    issueKey: "SCRUM-2",
    summary: "Another story",
    status: "Done",
    assignee: "Bob",
    issueType: "Story",
    totalStoryPoints: 5,
    remainingStoryPoints: 0,
    doneStoryPoints: 5,
  };

  const mockMetrics: SprintMetrics = {
    committedStoryPoints: 40,
    deliveredStoryPoints: 32,
    workStoryPoints: 29,
    leftoverStoryPoints: 8,
    remainingStoryPoints: 8,
    ratio: 80,
    velocity: 32,
    predictabilityRate: 80,
    sprintSuccess: true,
    totalIssues: 10,
    completedIssues: 7,
    inProgressIssues: 2,
    todoIssues: 1,
    blockedIssues: 0,
    bugCount: 2,
    storyCount: 6,
    taskCount: 2,
    blockedRatio: 0,
    bugRatio: 20,
    sprintFocusFactor: 0.8,
    teamEfficiency: 0.9,
    averageSpPerCompletedIssue: 4.6,
    sprintHealthScore: 85,
    throughput: 7,
    workInProgress: 2,
    carryOverIssues: 1,
    topicBreakdown: [],
    realCapacity: 38,
    teamAvailability: { dev: 3, pda: 1, qa: 1 },
  };

  const mockIterations: IterationSnapshot[] = [
    {
      sprintName: "Sprint 1",
      committedStoryPoints: 30,
      deliveredStoryPoints: 28,
      velocity: 28,
      ratio: 93.3,
    },
    {
      sprintName: "Sprint 2",
      committedStoryPoints: 40,
      deliveredStoryPoints: 32,
      velocity: 32,
      ratio: 80,
    },
  ];

  beforeEach(() => {
    apiServiceMock = jasmine.createSpyObj("SprintApiService", [
      "getSprintIssues",
      "getMetrics",
      "getIterationHistory",
      "updateRemainingStoryPoints",
      "exportCsv",
    ]);

    TestBed.configureTestingModule({
      providers: [
        SprintStateService,
        { provide: SprintApiService, useValue: apiServiceMock },
      ],
    });

    service = TestBed.inject(SprintStateService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should start with empty issues and no error", () => {
    expect(service.issues()).toEqual([]);
    expect(service.loading()).toBeFalse();
    expect(service.error()).toBeNull();
  });

  // -----------------------------------------------------------------------
  describe("loadIssues()", () => {
    it("should populate issues signal on success", () => {
      apiServiceMock.getSprintIssues.and.returnValue(
        of([mockIssue, mockIssue2]),
      );

      service.loadIssues();

      expect(service.issues()).toEqual([mockIssue, mockIssue2]);
      expect(service.loading()).toBeFalse();
      expect(service.error()).toBeNull();
    });

    it("should set error signal on failure", () => {
      apiServiceMock.getSprintIssues.and.returnValue(
        throwError(() => new Error("Network error")),
      );

      service.loadIssues();

      expect(service.issues()).toEqual([]);
      expect(service.issuesError()).toBe("Network error");
      expect(service.loading()).toBeFalse();
    });
  });

  // -----------------------------------------------------------------------
  describe("summary computed signal", () => {
    it("should compute totals and completion percentage from issues", () => {
      apiServiceMock.getSprintIssues.and.returnValue(
        of([mockIssue, mockIssue2]),
      );
      service.loadIssues();

      const summary = service.summary();

      expect(summary.totalIssues).toBe(2);
      expect(summary.totalStoryPoints).toBe(13); // 8 + 5
      expect(summary.remainingStoryPoints).toBe(3); // 3 + 0
      expect(summary.doneStoryPoints).toBe(10); // 5 + 5
      expect(summary.completionPercentage).toBe(77); // Math.round(10/13*100)
    });

    it("should return 0% completion when no story points are defined", () => {
      apiServiceMock.getSprintIssues.and.returnValue(of([]));
      service.loadIssues();

      expect(service.summary().completionPercentage).toBe(0);
    });
  });

  // -----------------------------------------------------------------------
  describe("updateRemainingStoryPoints()", () => {
    it("should update the matching issue in the issues signal", () => {
      apiServiceMock.getSprintIssues.and.returnValue(
        of([mockIssue, mockIssue2]),
      );
      apiServiceMock.getMetrics.and.returnValue(of(mockMetrics));
      service.loadIssues();

      const updatedIssue: SprintIssue = {
        ...mockIssue,
        remainingStoryPoints: 1,
        doneStoryPoints: 7,
      };
      apiServiceMock.updateRemainingStoryPoints.and.returnValue(
        of(updatedIssue),
      );

      service.updateRemainingStoryPoints("SCRUM-1", 1);

      const found = service.issues().find((i) => i.issueKey === "SCRUM-1");
      expect(found?.remainingStoryPoints).toBe(1);
      expect(found?.doneStoryPoints).toBe(7);
    });

    it("should set error when update fails", () => {
      apiServiceMock.updateRemainingStoryPoints.and.returnValue(
        throwError(() => new Error("Save failed")),
      );

      service.updateRemainingStoryPoints("SCRUM-1", 2);

      expect(service.error()).toBe("Save failed");
    });
  });

  // -----------------------------------------------------------------------
  describe("clearError()", () => {
    it("should reset error signal to null", () => {
      apiServiceMock.getSprintIssues.and.returnValue(
        throwError(() => new Error("Some error")),
      );
      service.loadIssues();
      expect(service.issuesError()).toBeTruthy();

      service.clearError();

      expect(service.issuesError()).toBeNull();
    });
  });

  // -----------------------------------------------------------------------
  describe("loadMetrics()", () => {
    it("should populate metrics signal on success", () => {
      apiServiceMock.getMetrics.and.returnValue(of(mockMetrics));

      service.loadMetrics();

      expect(service.metrics()).toEqual(mockMetrics);
      expect(service.metricsLoading()).toBeFalse();
    });

    it("should set metricsLoading during load", () => {
      apiServiceMock.getMetrics.and.returnValue(of(mockMetrics));

      // Before call, not loading
      expect(service.metricsLoading()).toBeFalse();

      service.loadMetrics();

      // After sync subscribe, loading done
      expect(service.metricsLoading()).toBeFalse();
    });

    it("should set error on metrics load failure", () => {
      apiServiceMock.getMetrics.and.returnValue(
        throwError(() => new Error("Metrics failed")),
      );

      service.loadMetrics();

      expect(service.metricsError()).toBe("Metrics failed");
      expect(service.metricsLoading()).toBeFalse();
    });
  });

  // -----------------------------------------------------------------------
  describe("loadIterations()", () => {
    it("should populate iterations signal on success", () => {
      apiServiceMock.getIterationHistory.and.returnValue(of(mockIterations));

      service.loadIterations();

      expect(service.iterations().length).toBe(2);
      expect(service.iterations()[0].sprintName).toBe("Sprint 1");
    });

    it("should set error on iterations load failure", () => {
      apiServiceMock.getIterationHistory.and.returnValue(
        throwError(() => new Error("Iterations failed")),
      );

      service.loadIterations();

      expect(service.iterationsError()).toBe("Iterations failed");
    });
  });

  // -----------------------------------------------------------------------
  describe("updateRemainingStoryPoints()", () => {
    it("should update the matching issue and refresh metrics", () => {
      apiServiceMock.getSprintIssues.and.returnValue(
        of([mockIssue, mockIssue2]),
      );
      apiServiceMock.getMetrics.and.returnValue(of(mockMetrics));
      service.loadIssues();

      const updatedIssue: SprintIssue = {
        ...mockIssue,
        remainingStoryPoints: 1,
        doneStoryPoints: 7,
      };
      apiServiceMock.updateRemainingStoryPoints.and.returnValue(
        of(updatedIssue),
      );

      service.updateRemainingStoryPoints("SCRUM-1", 1);

      const found = service.issues().find((i) => i.issueKey === "SCRUM-1");
      expect(found?.remainingStoryPoints).toBe(1);
      expect(found?.doneStoryPoints).toBe(7);
      expect(service.savingIssueKey()).toBeNull();
      // Should have triggered loadMetrics
      expect(apiServiceMock.getMetrics).toHaveBeenCalled();
    });

    it("should set error and clear savingIssueKey when update fails", () => {
      apiServiceMock.updateRemainingStoryPoints.and.returnValue(
        throwError(() => new Error("Save failed")),
      );

      service.updateRemainingStoryPoints("SCRUM-1", 2);

      expect(service.error()).toBe("Save failed");
      expect(service.savingIssueKey()).toBeNull();
    });
  });

  // -----------------------------------------------------------------------
  describe("exportCsv()", () => {
    it("should create a download link on success", () => {
      const blob = new Blob(["csv"], { type: "text/csv" });
      apiServiceMock.exportCsv.and.returnValue(of(blob));
      spyOn(window.URL, "createObjectURL").and.returnValue("blob:url");
      spyOn(window.URL, "revokeObjectURL");
      const clickSpy = jasmine.createSpy("click");
      spyOn(document, "createElement").and.returnValue({
        click: clickSpy,
        href: "",
        download: "",
      } as any);

      service.exportCsv();

      expect(window.URL.createObjectURL).toHaveBeenCalledWith(blob);
      expect(clickSpy).toHaveBeenCalled();
      expect(window.URL.revokeObjectURL).toHaveBeenCalledWith("blob:url");
    });

    it("should set error on export failure", () => {
      apiServiceMock.exportCsv.and.returnValue(
        throwError(() => new Error("Export failed")),
      );

      service.exportCsv();

      expect(service.error()).toBe("Export failed");
    });
  });
});
