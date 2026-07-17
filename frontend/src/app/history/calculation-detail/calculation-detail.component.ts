import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CalculationService, SavedCalculation } from '../../core/calculation.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { ResultDisplayComponent } from '../../calculator/result-display/result-display.component';

@Component({
  selector: 'app-calculation-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule,
    ResultDisplayComponent
  ],
  templateUrl: './calculation-detail.component.html',
  styleUrls: ['./calculation-detail.component.css']
})
export class CalculationDetailComponent implements OnInit {
  calculation: SavedCalculation | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private calculationService: CalculationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loading = true;
    const id = Number(this.route.snapshot.paramMap.get('id'));
    
    if (isNaN(id)) {
      this.router.navigate(['/history']);
      return;
    }
    
    this.calculationService.getCalculation(id).subscribe({
      next: (calculation) => {
        this.calculation = calculation;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(
          error.error?.message || 'Failed to load calculation details',
          'Close',
          { duration: 5000 }
        );
        this.router.navigate(['/history']);
      }
    });
  }

  deleteCalculation(): void {
    if (!this.calculation) return;
    
    if (!confirm('Are you sure you want to delete this calculation?')) {
      return;
    }
    
    this.calculationService.deleteCalculation(this.calculation.id).subscribe({
      next: () => {
        this.snackBar.open('Calculation deleted successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/history']);
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

  goBack(): void {
    this.router.navigate(['/history']);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }
  
  getInputEntries(input: any): Array<[string, any]> {
    return Object.entries(input);
  }
  
  formatInputLabel(key: string): string {
    // Convert camelCase to Title Case with spaces
    const result = key.replace(/([A-Z])/g, ' $1');
    return result.charAt(0).toUpperCase() + result.slice(1);
  }
}