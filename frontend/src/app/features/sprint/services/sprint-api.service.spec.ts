import { TestBed } from "@angular/core/testing";
import {
  HttpClientTestingModule,
  HttpTestingController,
} from "@angular/common/http/testing";
import { SprintApiService } from "./sprint-api.service";
import {
  SprintIssue,
  SprintMetrics,
  IterationSnapshot,
  UpdateRemainingSpRequest,
} from "../models/sprint-issue.model";
import { environment } from "../../../../environments/environment";

describe("SprintApiService", () => {
  let service: SprintApiService;
  let httpMock: HttpTestingController;

  const BASE = `${environment.apiBaseUrl}/api/v1`;

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
    topicBreakdown: [{ topic: "Auth", storyPoints: 20 }],
    capacity: { plannedCapacity: 40, realCapacity: 38 },
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
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SprintApiService],
    });
    service = TestBed.inject(SprintApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  // -----------------------------------------------------------------------
  describe("getSprintIssues()", () => {
    it("should GET /api/v1/issues and return issues array", () => {
      service.getSprintIssues().subscribe((issues) => {
        expect(issues).toEqual([mockIssue]);
      });

      const req = httpMock.expectOne(`${BASE}/issues`);
      expect(req.request.method).toBe("GET");
      req.flush([mockIssue]);
    });
  });

  // -----------------------------------------------------------------------
  describe("getMetrics()", () => {
    it("should GET /api/v1/metrics and return SprintMetrics", () => {
      service.getMetrics().subscribe((m) => {
        expect(m).toEqual(mockMetrics);
        expect(m.committedStoryPoints).toBe(40);
        expect(m.topicBreakdown.length).toBe(1);
      });

      const req = httpMock.expectOne(`${BASE}/metrics`);
      expect(req.request.method).toBe("GET");
      req.flush(mockMetrics);
    });
  });

  // -----------------------------------------------------------------------
  describe("getIterationHistory()", () => {
    it("should GET /api/v1/metrics/iterations and return snapshots", () => {
      service.getIterationHistory().subscribe((data) => {
        expect(data.length).toBe(2);
        expect(data[0].sprintName).toBe("Sprint 1");
        expect(data[1].velocity).toBe(32);
      });

      const req = httpMock.expectOne(`${BASE}/metrics/iterations`);
      expect(req.request.method).toBe("GET");
      req.flush(mockIterations);
    });
  });

  // -----------------------------------------------------------------------
  describe("updateRemainingStoryPoints()", () => {
    it("should POST /api/v1/issues/update with correct body", () => {
      const request: UpdateRemainingSpRequest = {
        issueKey: "SCRUM-1",
        remainingStoryPoints: 2,
      };

      service.updateRemainingStoryPoints(request).subscribe((issue) => {
        expect(issue.remainingStoryPoints).toBe(2);
      });

      const req = httpMock.expectOne(`${BASE}/issues/update`);
      expect(req.request.method).toBe("POST");
      expect(req.request.body).toEqual(request);
      req.flush({ ...mockIssue, remainingStoryPoints: 2, doneStoryPoints: 6 });
    });
  });

  // -----------------------------------------------------------------------
  describe("exportCsv()", () => {
    it("should GET /api/v1/export/csv and return a Blob", () => {
      service.exportCsv().subscribe((blob) => {
        expect(blob).toBeInstanceOf(Blob);
      });

      const req = httpMock.expectOne(`${BASE}/export/csv`);
      expect(req.request.method).toBe("GET");
      expect(req.request.responseType).toBe("blob");
      req.flush(new Blob(["csv content"], { type: "text/csv" }));
    });
  });
});
