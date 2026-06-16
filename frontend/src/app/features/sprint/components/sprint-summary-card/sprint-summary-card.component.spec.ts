import { ComponentFixture, TestBed } from "@angular/core/testing";
import { SprintSummaryCardComponent } from "./sprint-summary-card.component";
import { SprintSummary } from "../../models/sprint-issue.model";

describe("SprintSummaryCardComponent", () => {
  let component: SprintSummaryCardComponent;
  let fixture: ComponentFixture<SprintSummaryCardComponent>;

  const mockSummary: SprintSummary = {
    totalIssues: 10,
    totalStoryPoints: 40,
    remainingStoryPoints: 8,
    doneStoryPoints: 32,
    completionPercentage: 80,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SprintSummaryCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SprintSummaryCardComponent);
    component = fixture.componentInstance;
    component.summary = mockSummary;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should render total issues", () => {
    const el: HTMLElement = fixture.nativeElement;
    const values = el.querySelectorAll(".kpi-value");
    expect(values[0].textContent?.trim()).toBe("10");
  });

  it("should render total story points", () => {
    const el: HTMLElement = fixture.nativeElement;
    const values = el.querySelectorAll(".kpi-value");
    expect(values[1].textContent?.trim()).toBe("40");
  });

  it("should render done story points", () => {
    const el: HTMLElement = fixture.nativeElement;
    const values = el.querySelectorAll(".kpi-value");
    expect(values[2].textContent?.trim()).toBe("32");
  });

  it("should render remaining story points", () => {
    const el: HTMLElement = fixture.nativeElement;
    const values = el.querySelectorAll(".kpi-value");
    expect(values[3].textContent?.trim()).toBe("8");
  });

  it("should render completion percentage", () => {
    const el: HTMLElement = fixture.nativeElement;
    const values = el.querySelectorAll(".kpi-value");
    expect(values[4].textContent?.trim()).toBe("80%");
  });

  it("should render the progress bar", () => {
    const fill: HTMLElement = fixture.nativeElement.querySelector(
      ".progress-bar__fill",
    );
    expect(fill).toBeTruthy();
    expect(fill.style.width).toBe("80%");
  });

  it("should update when summary changes", () => {
    component.summary = { ...mockSummary, completionPercentage: 50 };
    fixture.detectChanges();
    const fill: HTMLElement = fixture.nativeElement.querySelector(
      ".progress-bar__fill",
    );
    expect(fill.style.width).toBe("50%");
  });

  it("should show 0% for zero completion", () => {
    component.summary = { ...mockSummary, completionPercentage: 0 };
    fixture.detectChanges();
    const values = fixture.nativeElement.querySelectorAll(".kpi-value");
    expect(values[4].textContent?.trim()).toBe("0%");
  });
});
