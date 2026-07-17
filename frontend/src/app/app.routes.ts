import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth.guard';

export const routes: Routes = [
  { 
    path: 'login', 
    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent)  
  },
  { 
    path: 'register', 
    loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent)  
  },
  { 
    path: 'calculator', 
    loadComponent: () => import('./calculator/calculator-form/calculator-form.component')
      .then(m => m.CalculatorFormComponent),
    canActivate: [AuthGuard]  
  },
  { 
    path: 'history', 
    loadComponent: () => import('./history/history-list/history-list.component')
      .then(m => m.HistoryListComponent),
    canActivate: [AuthGuard]  
  },
  { 
    path: 'history/:id', 
    loadComponent: () => import('./history/calculation-detail/calculation-detail.component')
      .then(m => m.CalculationDetailComponent),
    canActivate: [AuthGuard]  
  },
  { path: '', redirectTo: '/calculator', pathMatch: 'full' },
  { path: '**', redirectTo: '/calculator' }
];