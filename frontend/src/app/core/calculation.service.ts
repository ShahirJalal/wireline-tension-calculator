import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

export interface CalculationInput {
  toolstringDepth: number;
  pltToolLength: number;
  lineWtPer1000ft: number;
  wellDeviation: number;
  wirelineOd: number;
  surfaceGasFlowrate: number;
  surfaceOilFlowrate: number;
  oilSolnGor: number;
  downholeTemp: number;
  gasSpecificGravity: number;
  surfacePressure: number;
  bo: number;
  calcDholeDensityOil: number;
  downholeDensityWater: number;
  surfacePull: number;
  pressControlFriction: number;
  tubingId: number;
  lineFrictionFactor: number;
  tubingDepth: number;
  casingId: number;
  toolstringWt: number;
  toolOd: number;
  toolstringFriction: number;
}

export interface CalculationResult {
  pressureAtToolstring: number;
  surfaceWaterFlowrate: number;
  netForceOnWire: number;
  downholeDensityGas: number;
  netForceOfToolstring: number;
  netForceOfSystem: number;
  lineWeightDeviatedLessBuoyancy: number;
  toolWeightDeviatedLessBuoyancy: number;
  linePullAtSurface: number;
  wellheadPress: number;
  pressControlFriction: number;
  lineForce: number;
  toolForce: number;
  positiveDownwardForces: number;
  counterForces: number;
}

export interface SavedCalculation {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  input?: CalculationInput;
  result?: CalculationResult;
}

export interface CalculationSaveRequest {
  name: string;
  description: string;
  input: CalculationInput;
}

@Injectable({
  providedIn: 'root'
})
export class CalculationService {
  constructor(private apiService: ApiService) { }

  // Perform calculation without saving
  calculate(input: CalculationInput): Observable<CalculationResult> {
    return this.apiService.post<CalculationResult>('calculations/calculate', input);
  }

  // Save a calculation
  saveCalculation(data: CalculationSaveRequest): Observable<SavedCalculation> {
    return this.apiService.post<SavedCalculation>('calculations', data);
  }

  // Get all calculations for the current user
  getCalculations(): Observable<SavedCalculation[]> {
    return this.apiService.get<SavedCalculation[]>('calculations');
  }

  // Get a specific calculation by ID
  getCalculation(id: number): Observable<SavedCalculation> {
    return this.apiService.get<SavedCalculation>(`calculations/${id}`);
  }

  // Delete a calculation
  deleteCalculation(id: number): Observable<any> {
    return this.apiService.delete<any>(`calculations/${id}`);
  }
}