import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CalculationService, CalculationInput, CalculationResult } from '../../core/calculation.service';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ResultDisplayComponent } from '../result-display/result-display.component';

@Component({
  selector: 'app-calculator-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatSnackBarModule,
    MatExpansionModule,
    MatDialogModule,
    ResultDisplayComponent
  ],
  templateUrl: './calculator-form.component.html',
  styleUrls: ['./calculator-form.component.css']
})
export class CalculatorFormComponent {
  calculatorForm: FormGroup;
  saveForm: FormGroup;
  loading = false;
  calculationResult: CalculationResult | null = null;
  showSaveForm = false;

  constructor(
    private formBuilder: FormBuilder,
    private calculationService: CalculationService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.calculatorForm = this.formBuilder.group({
      toolstringDepth: [null, [Validators.required]],
      pltToolLength: [null, [Validators.required]],
      lineWtPer1000ft: [null, [Validators.required]],
      wellDeviation: [null, [Validators.required]],
      wirelineOd: [null, [Validators.required]],
      surfaceGasFlowrate: [null, [Validators.required]],
      surfaceOilFlowrate: [null, [Validators.required]],
      oilSolnGor: [null, [Validators.required]],
      downholeTemp: [null, [Validators.required]],
      gasSpecificGravity: [null, [Validators.required]],
      surfacePressure: [null, [Validators.required]],
      bo: [null, [Validators.required]],
      calcDholeDensityOil: [null, [Validators.required]],
      downholeDensityWater: [null, [Validators.required]],
      surfacePull: [null, [Validators.required]],
      pressControlFriction: [null, [Validators.required]],
      tubingId: [null, [Validators.required]],
      lineFrictionFactor: [null, [Validators.required]],
      tubingDepth: [null, [Validators.required]],
      casingId: [null, [Validators.required]],
      toolstringWt: [null, [Validators.required]],
      toolOd: [null, [Validators.required]],
      toolstringFriction: [null, [Validators.required]]
    });

    this.saveForm = this.formBuilder.group({
      name: ['', [Validators.required]],
      description: ['']
    });
  }

  calculate(): void {
    if (this.calculatorForm.invalid) {
      this.snackBar.open('Please fill in all required fields', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;
    const input: CalculationInput = this.calculatorForm.value;

    this.calculationService.calculate(input).subscribe({
      next: (result) => {
        this.loading = false;
        this.calculationResult = result;
        this.showSaveForm = true;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(
          error.error?.message || 'Calculation failed. Please try again.',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  saveCalculation(): void {
    if (this.saveForm.invalid || !this.calculationResult) {
      return;
    }

    this.loading = true;
    
    this.calculationService.saveCalculation({
      name: this.saveForm.value.name,
      description: this.saveForm.value.description,
      input: this.calculatorForm.value
    }).subscribe({
      next: () => {
        this.loading = false;
        this.snackBar.open('Calculation saved successfully', 'Close', { duration: 3000 });
        this.showSaveForm = false;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(
          error.error?.message || 'Failed to save calculation. Please try again.',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  loadSampleData() {
    this.calculatorForm.patchValue({
      toolstringDepth: 6237.37,
      pltToolLength: 41.71,
      lineWtPer1000ft: 159,
      wellDeviation: 41.62,
      wirelineOd: 0.281,
      surfaceGasFlowrate: 0.7574,
      surfaceOilFlowrate: 1453,
      oilSolnGor: 440,
      downholeTemp: 250,
      gasSpecificGravity: 0.72,
      surfacePressure: 120,
      bo: 1.2,
      calcDholeDensityOil: 0.777,
      downholeDensityWater: 1,
      surfacePull: 25,
      pressControlFriction: 25,
      tubingId: 2.992,
      lineFrictionFactor: 0.007,
      tubingDepth: 6265.39,
      casingId: 6.184,
      toolstringWt: 175,
      toolOd: 1.693,
      toolstringFriction: 0.003
    });
  }

  clearForm() {
    this.calculatorForm.reset();
    this.calculationResult = null;
    this.showSaveForm = false;
  }
}