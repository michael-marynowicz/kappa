import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

interface Feature {
  icon: string;
  title: string;
  desc: string;
  color: string;
}

@Component({
  selector: "app-home",
  standalone: true,
  imports: [RouterLink],
  templateUrl: "./home.component.html",
  styleUrls: ["./home.component.scss"],
})
export class HomeComponent {
  readonly year = new Date().getFullYear();

  scrollTo(id: string, event: Event): void {
    event.preventDefault();
    document
      .getElementById(id)
      ?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  readonly features: Feature[] = [
    {
      icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>`,
      title: "Sprint Dashboard",
      desc: "Real-time board with issue tracking, story-point progress, and sprint health indicators — all pulled live from your Jira.",
      color: "indigo",
    },
    {
      icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>`,
      title: "Backlog View",
      desc: "Full visibility into your product backlog with issue status, epics, assignees, and remaining story points at a glance.",
      color: "blue",
    },
    {
      icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>`,
      title: "Velocity & Metrics",
      desc: "Sprint velocity trends, burndown charts, and historical comparisons to quantify continuous improvement over time.",
      color: "violet",
    },
    {
      icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
      title: "Capacity Planning",
      desc: "Track team availability, days-off per sprint, and role-based capacity — so you can plan sprints with confidence.",
      color: "teal",
    },
    {
      icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>`,
      title: "CSV Export",
      desc: "Export sprint reports and capacity plans to CSV in one click — ready for stakeholders, retros, or archiving.",
      color: "amber",
    },
    {
      icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>`,
      title: "Team Invitations",
      desc: "Invite teammates by email, assign Admin / Member / Viewer roles, and manage your whole organization from one screen.",
      color: "pink",
    },
  ];
}
