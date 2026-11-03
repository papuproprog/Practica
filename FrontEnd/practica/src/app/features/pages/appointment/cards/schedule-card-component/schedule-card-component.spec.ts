import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ScheduleCardComponent } from './schedule-card-component';

describe('ScheduleCardComponent', () => {
  let component: ScheduleCardComponent;
  let fixture: ComponentFixture<ScheduleCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduleCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ScheduleCardComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
