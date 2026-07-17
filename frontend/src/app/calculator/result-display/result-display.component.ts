import { Component, Input } from '@angular/core';
import { CalculationResult } from '../../core/calculation.service';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-result-display',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatDividerModule
  ],
  templateUrl: './result-display.component.html',
  styleUrls: ['./result-display.component.css']
})
export class ResultDisplayComponent {
  @Input() result: CalculationResult | null = null;

  getForceType(value: number): string {
    return value > 0 ? 'Drag' : 'Lift';
  }
}