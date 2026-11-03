import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DateHighlightComponent } from './date-highlight-component';

describe('DateHighlightComponent', () => {
  let component: DateHighlightComponent;
  let fixture: ComponentFixture<DateHighlightComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DateHighlightComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DateHighlightComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
