import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatusBadgeComponent } from './status-badge.component';

describe('StatusBadgeComponent', () => {
  let fixture: ComponentFixture<StatusBadgeComponent>;
  let component: StatusBadgeComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusBadgeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(StatusBadgeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.status = 'Done';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should return badge--done for "Done" status', () => {
    component.status = 'Done';
    expect(component.badgeClass).toBe('badge--done');
  });

  it('should return badge--in-progress for "In Progress" status', () => {
    component.status = 'In Progress';
    expect(component.badgeClass).toBe('badge--in-progress');
  });

  it('should return badge--in-review for "In Review" status', () => {
    component.status = 'In Review';
    expect(component.badgeClass).toBe('badge--in-review');
  });

  it('should return badge--to-do for "To Do" status', () => {
    component.status = 'To Do';
    expect(component.badgeClass).toBe('badge--to-do');
  });

  it('should return badge--default for unknown status', () => {
    component.status = 'Blocked';
    expect(component.badgeClass).toBe('badge--default');
  });

  it('should render the status text in the DOM', () => {
    component.status = 'In Progress';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement.querySelector('.badge');
    expect(el.textContent?.trim()).toBe('In Progress');
  });
});
