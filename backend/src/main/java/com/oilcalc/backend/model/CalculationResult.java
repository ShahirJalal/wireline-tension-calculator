package com.oilcalc.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResult {
    private double pressureAtToolstring;
    private double surfaceWaterFlowrate;
    private double netForceOnWire;
    private double downholeDensityGas;
    private double netForceOfToolstring;
    private double netForceOfSystem;
    private double lineWeightDeviatedLessBuoyancy;
    private double toolWeightDeviatedLessBuoyancy;
    private double linePullAtSurface;
    private double wellheadPress;
    private double pressControlFriction;
    private double lineForce;
    private double toolForce;
    private double positiveDownwardForces;
    private double counterForces;
}