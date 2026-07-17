import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalculationDetailComponent } from './calculation-detail.component';

describe('CalculationDetailComponent', () => {
  let component: CalculationDetailComponent;
  let fixture: ComponentFixture<CalculationDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalculationDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CalculationDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
