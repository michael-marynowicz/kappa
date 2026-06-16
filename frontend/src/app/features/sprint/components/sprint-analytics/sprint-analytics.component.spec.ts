import { ComponentFixture, TestBed } from "@angular/core/testing";
import { SprintAnalyticsComponent } from "./sprint-analytics.component";
import {
  SprintMetrics,
  IterationSnapshot,
} from "../../models/sprint-issue.model";
import { SimpleChange } from "@angular/core";

describe("SprintAnalyticsComponent", () => {
  let component: SprintAnalyticsComponent;
  let fixture: ComponentFixture<SprintAnalyticsComponent>;

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
    topicBreakdown: [
      { topic: "Auth", storyPoints: 20 },
      { topic: "UI", storyPoints: 12 },
    ],
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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SprintAnalyticsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SprintAnalyticsComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  // -----------------------------------------------------------------------
  describe("ngOnChanges with metrics", () => {
    beforeEach(() => {
      component.metrics = mockMetrics;
      component.iterations = mockIterations;
      component.ngOnChanges({
        metrics: new SimpleChange(null, mockMetrics, true),
        iterations: new SimpleChange(null, mockIterations, true),
      });
    });

    it("should build velocity bars", () => {
      expect(component.velocityBars.length).toBe(4);
      expect(component.velocityBars[0].label).toBe("Committed");
      expect(component.velocityBars[0].value).toBe(40);
      expect(component.velocityBars[1].label).toBe("Delivered");
      expect(component.velocityBars[1].value).toBe(32);
    });

    it("should build topic donut segments", () => {
      expect(component.topicSegments.length).toBe(2);
      expect(component.totalTopicSP).toBe(32);
      expect(component.topicSegments[0].label).toBe("Auth");
      expect(component.topicSegments[0].storyPoints).toBe(20);
    });

    it("should compute capacity data", () => {
      expect(component.capacityMax).toBe(40);
      expect(component.capacityDelta).toBe(-2);
      expect(component.capacityDeltaColor).toBe("#f87171");
    });

    it("should compute total EFT", () => {
      expect(component.totalEft).toBe(5);
    });

    it("should build iteration bars", () => {
      expect(component.iterationBars.length).toBe(2);
      expect(component.iterationBars[0].sprint).toBe("Sprint 1");
      expect(component.iterationBars[0].committed).toBe(30);
      expect(component.iterationBars[1].ratio).toBe("80.0");
    });
  });

  // -----------------------------------------------------------------------
  describe("barWidth()", () => {
    it("should calculate percentage", () => {
      expect(component.barWidth(50, 100)).toBe(50);
    });

    it("should cap at 100%", () => {
      expect(component.barWidth(150, 100)).toBe(100);
    });

    it("should return 0 when max is 0", () => {
      expect(component.barWidth(50, 0)).toBe(0);
    });
  });

  // -----------------------------------------------------------------------
  describe("ratioColor()", () => {
    it("should return green for ratio >= 85", () => {
      expect(component.ratioColor(90)).toBe("#34d399");
    });

    it("should return yellow for ratio >= 65", () => {
      expect(component.ratioColor(70)).toBe("#fbbf24");
    });

    it("should return red for ratio < 65", () => {
      expect(component.ratioColor(50)).toBe("#f87171");
    });

    it("should return muted for null", () => {
      expect(component.ratioColor(null)).toBe("#8b92a8");
    });
  });

  // -----------------------------------------------------------------------
  describe("edge cases", () => {
    it("should handle empty topic breakdown", () => {
      const noTopics = { ...mockMetrics, topicBreakdown: [] };
      component.metrics = noTopics;
      component.ngOnChanges({
        metrics: new SimpleChange(null, noTopics, true),
      });

      expect(component.topicSegments.length).toBe(0);
      expect(component.totalTopicSP).toBe(0);
    });

    it("should handle null metrics gracefully", () => {
      component.metrics = null;
      component.ngOnChanges({
        metrics: new SimpleChange(mockMetrics, null, false),
      });

      // Should not throw
      expect(component.velocityBars.length).toBe(0);
    });

    it("should handle empty iterations", () => {
      component.metrics = mockMetrics;
      component.iterations = [];
      component.ngOnChanges({
        iterations: new SimpleChange(null, [], true),
      });

      expect(component.iterationBars.length).toBe(0);
    });

    it("should handle iteration with null ratio", () => {
      const iters: IterationSnapshot[] = [
        {
          sprintName: "S1",
          committedStoryPoints: 10,
          deliveredStoryPoints: 0,
          velocity: 0,
          ratio: null,
        },
      ];
      component.metrics = mockMetrics;
      component.iterations = iters;
      component.ngOnChanges({
        iterations: new SimpleChange(null, iters, true),
      });

      expect(component.iterationBars[0].ratio).toBe("—");
    });

    it("should handle positive capacity delta with green color", () => {
      const positiveCap = {
        ...mockMetrics,
        capacity: { plannedCapacity: 30, realCapacity: 35 },
      };
      component.metrics = positiveCap;
      component.ngOnChanges({
        metrics: new SimpleChange(null, positiveCap, true),
      });

      expect(component.capacityDelta).toBe(5);
      expect(component.capacityDeltaColor).toBe("#34d399");
    });
  });
});
