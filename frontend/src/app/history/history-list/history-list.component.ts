import { Component, OnInit } from '@angular/core';
import { CalculationService, SavedCalculation } from '../../core/calculation.service';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-history-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './history-list.component.html',
  styleUrls: ['./history-list.component.css']
})
export class HistoryListComponent implements OnInit {
  calculations: SavedCalculation[] = [];
  loading = false;

  constructor(
    private calculationService: CalculationService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCalculations();
  }

  loadCalculations(): void {
    this.loading = true;
    this.calculationService.getCalculations().subscribe({
      next: (calculations) => {
        this.calculations = calculations;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(
          error.error?.message || 'Failed to load calculations',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  viewCalculation(id: number): void {
    this.router.navigate(['/history', id]);
  }

  deleteCalculation(id: number, event: Event): void {
    event.stopPropagation();
    
    if (!confirm('Are you sure you want to delete this calculation?')) {
      return;
    }
    
    this.calculationService.deleteCalculation(id).subscribe({
      next: () => {
        this.calculations = this.calculations.filter(calc => calc.id !== id);
        this.snackBar.open('Calculation deleted successfully', 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || 'Failed to delete calculation',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }
}