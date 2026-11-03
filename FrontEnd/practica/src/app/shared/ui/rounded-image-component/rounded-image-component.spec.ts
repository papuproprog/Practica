import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RoundedImageComponent } from './rounded-image-component';

describe('RoundedImageComponent', () => {
  let component: RoundedImageComponent;
  let fixture: ComponentFixture<RoundedImageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoundedImageComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RoundedImageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
