import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LocalStatusComponent } from './local-status-component';

describe('LocalStatusComponent', () => {
  let component: LocalStatusComponent;
  let fixture: ComponentFixture<LocalStatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LocalStatusComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(LocalStatusComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
