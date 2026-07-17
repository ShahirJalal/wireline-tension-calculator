package com.oilcalc.backend.service;

import com.oilcalc.backend.model.Calculation;
import com.oilcalc.backend.model.CalculationInput;
import com.oilcalc.backend.model.CalculationResult;
import com.oilcalc.backend.model.User;
import com.oilcalc.backend.repository.CalculationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CalculationService {

    @Autowired
    private CalculationRepository calculationRepository;

    public List<Calculation> getCalculationsByUser(User user) {
        return calculationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Calculation getCalculationById(Long id, User user) {
        return calculationRepository.findById(id)
                .filter(calculation -> calculation.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Calculation not found or not authorized"));
    }

    public void deleteCalculation(Long id) {
        calculationRepository.deleteById(id);
    }

    public Calculation saveCalculation(CalculationInput input, User user, String name, String description) throws IOException {
        CalculationResult result = performCalculation(input);

        Calculation calculation = new Calculation();
        calculation.setUser(user);
        calculation.setName(name);
        calculation.setDescription(description);
        calculation.setInputFromObject(input);
        calculation.setResultFromObject(result);

        return calculationRepository.save(calculation);
    }

    public CalculationResult performCalculation(CalculationInput input) {
        // 1. Pressure at toolstring
        double pressureToolstring = (input.getToolstringDepth() * 0.433) + input.getSurfacePressure();

        // 2. Surface water flowrate (calculated)
        double surfaceWaterFlowrate = (24.0/100.0) * input.getSurfaceOilFlowrate();

        // 4. Calculate Downhole Density Gas
        double v13 = input.getDownholeTemp() + 460;
        double v3 = input.getGasSpecificGravity();
        double ab13 = 168 + 325 * v3 - 12.5 * Math.pow(v3, 2);
        double af13 = v13 / ab13;
        double z13 = 667 + 15 * v3 - 37.5 * Math.pow(v3, 2);
        double v14 = pressureToolstring;
        double ad13 = v14 / z13;
        double ab14 = (0.62 - 0.23 * af13) * ad13 + (0.066 / (af13 - 0.86) - 0.037) * Math.pow(ad13, 2);
        double ad14 = 0.32 / (Math.pow(10, 9 * (af13 - 1))) * Math.pow(ad13, 6);
        double af14 = 0.132 - 0.32 * Math.log10(af13);
        double ah14 = Math.pow(10, 0.3106 - 0.49 * af13 + 0.1824 * Math.pow(af13, 2));
        double z14 = 1.39 * Math.sqrt(af13 - 0.92) - 0.36 * af13 - 0.101;
        double v15 = z14 + (1 - z14) / Math.exp(ab14 + ad14) + af14 * Math.pow(ad13, ah14);
        double d8 = (0.043234 * pressureToolstring * input.getGasSpecificGravity()) / (v13 * v15);

        // 3. Net Force on Wire
        // Q8 components
        double v5 = (input.getDownholeTemp() + 60) / 2 + 460;
        double ab5 = 168 + 325 * v3 - 12.5 * Math.pow(v3, 2);
        double af5 = v5 / ab5;
        double z5 = 667 + 15 * v3 - 37.5 * Math.pow(v3, 2);
        double v6 = (input.getSurfacePressure() + pressureToolstring) / 2;
        double ad5 = v6 / z5;
        double ab6 = (0.62 - 0.23 * af5) * ad5 + (0.066 / (af5 - 0.86) - 0.037) * Math.pow(ad5, 2);
        double ad6 = 0.32 / (Math.pow(10, 9 * (af5 - 1))) * Math.pow(ad5, 6);
        double af6 = 0.132 - 0.32 * Math.log10(af5);
        double ah6 = Math.pow(10, 0.3106 - 0.49 * af5 + 0.1824 * Math.pow(af5, 2));
        double z6 = 1.39 * Math.sqrt(af5 - 0.92) - 0.36 * af5 - 0.101;
        double v7 = z6 + (1 - z6) / Math.exp(ab6 + ad6) + af6 * Math.pow(ad5, ah6);
        double v8 = 0.0283 * ((v5 * v7) / v6);

        double n5;
        if (input.getSurfaceGasFlowrate() > 0 &&
                (input.getOilSolnGor() * input.getSurfaceOilFlowrate()) < (input.getSurfaceGasFlowrate() * 1000)) {
            n5 = (((input.getSurfaceGasFlowrate() * 1000) - (input.getSurfaceOilFlowrate() * input.getOilSolnGor())) / 5.615) * v8;
        } else if (input.getSurfaceGasFlowrate() > 0) {
            n5 = ((input.getSurfaceGasFlowrate() * 1000) / 5.615) * v8;
        } else {
            n5 = 0;
        }

        double n6 = input.getSurfaceOilFlowrate() * input.getBo();
        double n7 = surfaceWaterFlowrate * 1.05;
        double n8 = n5 + n6 + n7;

        double s10 = (0.043234 * v6 * v3) / (v5 * v7);
        double p5 = n5 / n8;
        double q5 = p5 * s10;

        double p6 = n6 / n8;
        double q6 = p6 * input.getCalcDholeDensityOil();

        double p7 = n7 / n8;
        double q7 = p7 * input.getDownholeDensityWater();

        double q8;
        if ((input.getSurfaceGasFlowrate() + input.getSurfaceOilFlowrate() + surfaceWaterFlowrate) != 0) {
            q8 = q5 + q6 + q7;
        } else {
            q8 = d8;
        }

        // Q29 and E30
        double n29 = 0;
        if (input.getToolstringDepth() > input.getPltToolLength()) {
            n29 = ((input.getToolstringDepth() - input.getPltToolLength()) / 1000) *
                    input.getLineWtPer1000ft() *
                    Math.cos(input.getWellDeviation() * 0.017453292);
        }

        double q29 = n29 - ((((input.getWirelineOd() / 24) * (input.getWirelineOd() / 24) * 3.14152) *
                input.getToolstringDepth()) * (q8 * 62.43) * 0.85);
        double e30 = q29;

        // Additional forces
        double h27 = -input.getSurfacePull();
        double h28 = -((input.getWirelineOd() / 2) * (input.getWirelineOd() / 2) * 3.141516 * input.getSurfacePressure());
        double h29 = -input.getPressControlFriction();

        // Drag/Lift calculations (H30)
        double p25 = (n8 / 1.4 / (Math.pow(input.getTubingId(), 2) - Math.pow(input.getWirelineOd(), 2))) / 3.281 / 60;
        double s25 = input.getWirelineOd() * 0.0254;
        double o38;
        if (input.getToolstringDepth() > input.getPltToolLength()) {
            o38 = input.getTubingDepth();
        } else {
            o38 = input.getToolstringDepth() - input.getPltToolLength();
        }
        double p38 = o38 / 3.281;
        double s38;
        if (p25 < 0) {
            s38 = input.getLineFrictionFactor() * (q8 * 1000) * (Math.pow(p25, 2)) * 3.141516 * s25 * (p38 / 2);
        } else {
            s38 = input.getLineFrictionFactor() * (q8 * 1000) * ((Math.pow(p25, 2) * -1)) * 3.141516 * s25 * (p38 / 2);
        }
        double t38 = s38 * 0.2248;

        double n17 = n5 + n6 + n7;
        double p26 = (n17 / 1.4 / (Math.pow(input.getCasingId(), 2) - Math.pow(input.getWirelineOd(), 2))) / 3.281 / 60;
        double o39 = 0;
        if (input.getToolstringDepth() > (input.getTubingDepth() + input.getPltToolLength())) {
            o39 = input.getToolstringDepth() - (input.getTubingDepth() + input.getPltToolLength());
        }
        double p39 = o39 / 3.281;
        double s39;
        if (p26 < 0) {
            s39 = input.getLineFrictionFactor() * (q8 * 1000) * (Math.pow(p26, 2)) * 3.141516 * s25 * (p39 / 2);
        } else {
            s39 = input.getLineFrictionFactor() * (q8 * 1000) * ((Math.pow(p26, 2) * -1)) * 3.141516 * s25 * (p39 / 2);
        }
        double t39 = s39 * 0.2248;

        double u39 = t38 + t39;  // H30

        // Net Force on Wire
        double e30PlusForces = e30 + (h27 + h28 + h29 + u39);

        // 5. Net Force on Toolstring
        double n30 = input.getToolstringWt() * Math.cos(input.getWellDeviation() * 0.017453292);
        double q30 = n30 - ((((input.getToolOd() / 24) * (input.getToolOd() / 24) * 3.14152) *
                input.getPltToolLength()) * (q8 * 62.43));
        double e31 = q30;

        // Drag/Lift calculations (H31)
        double p23 = (n8 / 1.4 / (Math.pow(input.getTubingId(), 2) - Math.pow(input.getToolOd(), 2))) / 3.281 / 60;
        double s23 = input.getToolOd() * 0.0254;
        double o36 = 0;
        if (!(input.getToolstringDepth() > (input.getTubingDepth() + input.getPltToolLength()))) {
            o36 = input.getPltToolLength();
        }
        double p36 = o36 / 3.281;
        double s36;
        if (p23 < 0) {
            s36 = input.getToolstringFriction() * (q8 * 1000) * (Math.pow(p23, 2)) * 3.141516 * s23 * (p36 / 2);
        } else {
            s36 = input.getToolstringFriction() * (q8 * 1000) * ((-Math.pow(p23, 2))) * 3.141516 * s23 * (p36 / 2);
        }
        double t36 = s36 * 0.2248;

        double p24 = (n17 / 1.4 / (Math.pow(input.getCasingId(), 2) - Math.pow(input.getToolOd(), 2))) / 3.281 / 60;
        double o37 = 0;
        if (input.getToolstringDepth() > (input.getTubingDepth() + input.getPltToolLength())) {
            o37 = input.getPltToolLength();
        }
        double p37 = o37 / 3.281;
        double s37;
        if (p24 < 0) {
            s37 = input.getToolstringFriction() * (q8 * 1000) * (Math.pow(p24, 2)) * 3.141516 * s23 * (p37 / 2);
        } else {
            s37 = input.getToolstringFriction() * (q8 * 1000) * ((-Math.pow(p24, 2))) * 3.141516 * s23 * (p37 / 2);
        }
        double t37 = s37 * 0.2248;

        double u37 = t36 + t37;  // H31

        double e31PlusForces = e31 + u37;  // Net Force on Toolstring

        // 6. Net Force of System
        double e32 = q29 + q30;
        double h32 = h27 + h28 + h29 + u39 + u37;
        double e32PlusForces = e32 + h32;

        // Create the result object
        return CalculationResult.builder()
                .pressureAtToolstring(pressureToolstring)
                .surfaceWaterFlowrate(surfaceWaterFlowrate)
                .netForceOnWire(e30PlusForces)
                .downholeDensityGas(d8)
                .netForceOfToolstring(e31PlusForces)
                .netForceOfSystem(e32PlusForces)
                .lineWeightDeviatedLessBuoyancy(q29)
                .toolWeightDeviatedLessBuoyancy(q30)
                .linePullAtSurface(h27)
                .wellheadPress(h28)
                .pressControlFriction(h29)
                .lineForce(u39)
                .toolForce(u37)
                .positiveDownwardForces(e32)
                .counterForces(h32)
                .build();
    }
}