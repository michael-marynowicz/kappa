import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  trigger,
  transition,
  style,
  animate,
  query,
  stagger,
} from "@angular/animations";
import {
  SprintMetrics,
  IterationSnapshot,
  EpicGroup,
} from "../../models/sprint-issue.model";
import { PremiumOverlayComponent } from "../../../../shared/components/premium-overlay/premium-overlay.component";
import { TranslatePipe } from "../../../../shared/pipes/translate.pipe";

interface VelocityBar {
  label: string;
  value: number;
  max: number;
  color: string;
}
interface DonutSeg {
  label: string;
  storyPoints: number;
  issueCount: number;
  color: string;
  dash: string;
  offset: number;
  pct: string;
}
interface IterBar {
  sprint: string;
  committed: number;
  delivered: number;
  velocity: number;
  ratio: string;
  maxSP: number;
}

const TOPIC_COLORS = [
  "#60a5fa",
  "#34d399",
  "#f59e0b",
  "#a78bfa",
  "#f87171",
  "#818cf8",
  "#fb923c",
  "#2dd4bf",
  "#e879f9",
  "#fbbf24",
  "#38bdf8",
  "#4ade80",
  "#f472b6",
  "#c084fc",
  "#facc15",
];

export type ChartFocusView = "all" | "velocity" | "capacity" | "topics";

@Component({
  selector: "app-sprint-analytics",
  standalone: true,
  imports: [CommonModule, PremiumOverlayComponent, TranslatePipe],
  templateUrl: "./sprint-analytics.component.html",
  styleUrls: ["./sprint-analytics.component.scss"],
  animations: [
    trigger("cardAnimation", [
      transition(":enter", [
        style({ opacity: 0, transform: "scale(0.95) translateY(8px)" }),
        animate(
          "300ms 80ms cubic-bezier(0.4, 0, 0.2, 1)",
          style({ opacity: 1, transform: "scale(1) translateY(0)" }),
        ),
      ]),
      transition(":leave", [
        animate(
          "200ms cubic-bezier(0.4, 0, 0.6, 1)",
          style({ opacity: 0, transform: "scale(0.95) translateY(8px)" }),
        ),
      ]),
    ]),
  ],
})
export class SprintAnalyticsComponent implements OnChanges {
  @Input() metrics: SprintMetrics | null = null;
  @Input() iterations: IterationSnapshot[] = [];
  @Input() epicGroups: EpicGroup[] = [];
  @Input() iterationsGated = false;
  @Input() iterationsLoading = false;
  @Input() capacityGated = false;
  @Input() focusView: ChartFocusView = "all";

  /** Index of the currently hovered topic (-1 = none) */
  highlightedTopic = -1;

  /** Set of expanded epic names in topics focus view */
  expandedEpics = new Set<string>();

  /** Placeholder data shown behind premium overlay when iterations are gated */
  readonly placeholderIterations = [
    {
      sprint: "Sprint 18",
      committed: 45,
      delivered: 38,
      velocity: 38,
      ratio: "84.4%",
      pct: 84,
    },
    {
      sprint: "Sprint 19",
      committed: 50,
      delivered: 42,
      velocity: 42,
      ratio: "84.0%",
      pct: 84,
    },
    {
      sprint: "Sprint 20",
      committed: 48,
      delivered: 44,
      velocity: 44,
      ratio: "91.7%",
      pct: 92,
    },
    {
      sprint: "Sprint 21",
      committed: 52,
      delivered: 46,
      velocity: 46,
      ratio: "88.5%",
      pct: 88,
    },
  ];

  velocityBars: VelocityBar[] = [];
  topicSegments: DonutSeg[] = [];
  totalTopicSP = 0;
  totalTopicIssueCount = 0;
  capacityMax = 1;
  capacityDelta = 0;
  capacityDeltaColor = "#34d399";
  totalEft = 0;
  iterationBars: IterBar[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["metrics"] && this.metrics) {
      this.buildVelocity();
      this.buildTopicDonut();
      this.buildCapacity();
    }
    if (changes["iterations"] || changes["metrics"]) {
      this.buildIterations();
    }
    if (changes["focusView"]) {
      this.expandedEpics.clear();
    }
  }

  barWidth(value: number, max: number): number {
    return max > 0 ? Math.min(100, (value / max) * 100) : 0;
  }

  ratioColor(ratio: number | null): string {
    if (ratio == null) return "#8b92a8";
    if (ratio >= 85) return "#34d399";
    if (ratio >= 65) return "#fbbf24";
    return "#f87171";
  }

  toggleEpic(epicName: string): void {
    if (this.expandedEpics.has(epicName)) {
      this.expandedEpics.delete(epicName);
    } else {
      this.expandedEpics.add(epicName);
    }
  }

  isEpicExpanded(epicName: string): boolean {
    return this.expandedEpics.has(epicName);
  }

  epicColor(index: number): string {
    return TOPIC_COLORS[index % TOPIC_COLORS.length];
  }

  statusColor(status: string): string {
    switch (status) {
      case "Done":
        return "#34d399";
      case "In Progress":
      case "In Review":
        return "#60a5fa";
      default:
        return "#8b92a8";
    }
  }

  private buildVelocity(): void {
    const m = this.metrics!;
    const committed = m.committedStoryPoints ?? 0;
    const delivered = m.deliveredStoryPoints ?? 0;
    const work = m.workStoryPoints ?? 0;
    const leftover = m.leftoverStoryPoints ?? 0;
    const maxSP = Math.max(committed, delivered, 1);

    this.velocityBars = [
      {
        label: "Committed",
        value: committed,
        max: maxSP,
        color: "#60a5fa",
      },
      {
        label: "Delivered",
        value: delivered,
        max: maxSP,
        color: "#34d399",
      },
      { label: "Work", value: work, max: maxSP, color: "#a78bfa" },
      {
        label: "Leftover",
        value: leftover,
        max: maxSP,
        color: "#f87171",
      },
    ];
  }

  private buildTopicDonut(): void {
    const topics = this.metrics?.topicBreakdown ?? [];
    this.totalTopicSP = topics.reduce((s, t) => s + t.storyPoints, 0);
    this.totalTopicIssueCount = topics.reduce(
      (s, t) => s + (t.issueCount ?? 0),
      0,
    );

    if (this.totalTopicSP === 0) {
      this.topicSegments = [];
      return;
    }

    const circ = 2 * Math.PI * 48;
    let cumulative = 0;

    this.topicSegments = topics
      .filter((t) => t.storyPoints > 0)
      .map((t, i) => {
        const frac = t.storyPoints / this.totalTopicSP;
        const dash = `${frac * circ} ${circ}`;
        const offset = -cumulative * circ;
        cumulative += frac;
        return {
          label: t.topic,
          storyPoints: t.storyPoints,
          issueCount: t.issueCount ?? 0,
          color: TOPIC_COLORS[i % TOPIC_COLORS.length],
          dash,
          offset,
          pct: (frac * 100).toFixed(1) + "%",
        };
      });
  }

  private buildCapacity(): void {
    const cap = this.metrics?.capacity;
    const planned = cap?.plannedCapacity ?? 0;
    const real = cap?.realCapacity ?? 0;
    this.capacityMax = Math.max(planned, real, 1);
    this.capacityDelta = real - planned;
    this.capacityDeltaColor = this.capacityDelta >= 0 ? "#34d399" : "#f87171";

    const avail = this.metrics?.teamAvailability;
    this.totalEft = (avail?.dev ?? 0) + (avail?.pda ?? 0) + (avail?.qa ?? 0);
  }

  private buildIterations(): void {
    const iters = this.iterations ?? [];

    this.iterationBars = iters.map((it) => ({
      sprint: it.sprintName,
      committed: it.committedStoryPoints,
      delivered: it.deliveredStoryPoints,
      velocity: it.velocity,
      ratio:
        it.ratio !== null && it.ratio !== undefined ? it.ratio.toFixed(1) : "—",
      maxSP: Math.max(it.committedStoryPoints, it.deliveredStoryPoints, 1),
    }));
  }
}
