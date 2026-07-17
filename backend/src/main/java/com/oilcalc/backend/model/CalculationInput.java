package com.oilcalc.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculationInput {
    private double toolstringDepth;
    private double pltToolLength;
    private double lineWtPer1000ft;
    private double wellDeviation;
    private double wirelineOd;
    private double surfaceGasFlowrate;
    private double surfaceOilFlowrate;
    private double oilSolnGor;
    private double downholeTemp;
    private double gasSpecificGravity;
    private double surfacePressure;
    private double bo;
    private double calcDholeDensityOil;
    private double downholeDensityWater;
    private double surfacePull;
    private double pressControlFriction;
    private double tubingId;
    private double lineFrictionFactor;
    private double tubingDepth;
    private double casingId;
    private double toolstringWt;
    private double toolOd;
    private double toolstringFriction;
}