package com.oilcalc.backend.controller;

import com.oilcalc.backend.model.Calculation;
import com.oilcalc.backend.model.CalculationInput;
import com.oilcalc.backend.model.CalculationResult;
import com.oilcalc.backend.model.User;
import com.oilcalc.backend.service.CalculationService;
import com.oilcalc.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/calculations")
@CrossOrigin
public class CalculationController {

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<?> getAllCalculations() {
        User user = getCurrentUser();
        List<Calculation> calculations = calculationService.getCalculationsByUser(user);

        // Convert to simple format for response
        List<Map<String, Object>> response = calculations.stream()
                .map(calc -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", calc.getId());
                    item.put("name", calc.getName());
                    item.put("description", calc.getDescription());
                    item.put("createdAt", calc.getCreatedAt());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCalculation(@PathVariable Long id) {
        User user = getCurrentUser();
        Calculation calculation = calculationService.getCalculationById(id, user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", calculation.getId());
        response.put("name", calculation.getName());
        response.put("description", calculation.getDescription());
        response.put("createdAt", calculation.getCreatedAt());

        try {
            response.put("input", calculation.getInputAsObject());
            response.put("result", calculation.getResultAsObject());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error parsing calculation data");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createCalculation(@RequestBody Map<String, Object> request) {
        try {
            User user = getCurrentUser();

            // Extract fields
            String name = (String) request.getOrDefault("name", "Untitled Calculation");
            String description = (String) request.getOrDefault("description", "");

            // Convert input map to CalculationInput object
            Map<String, Object> inputMap = (Map<String, Object>) request.get("input");
            CalculationInput input = new CalculationInput(
                    ((Number) inputMap.get("toolstringDepth")).doubleValue(),
                    ((Number) inputMap.get("pltToolLength")).doubleValue(),
                    ((Number) inputMap.get("lineWtPer1000ft")).doubleValue(),
                    ((Number) inputMap.get("wellDeviation")).doubleValue(),
                    ((Number) inputMap.get("wirelineOd")).doubleValue(),
                    ((Number) inputMap.get("surfaceGasFlowrate")).doubleValue(),
                    ((Number) inputMap.get("surfaceOilFlowrate")).doubleValue(),
                    ((Number) inputMap.get("oilSolnGor")).doubleValue(),
                    ((Number) inputMap.get("downholeTemp")).doubleValue(),
                    ((Number) inputMap.get("gasSpecificGravity")).doubleValue(),
                    ((Number) inputMap.get("surfacePressure")).doubleValue(),
                    ((Number) inputMap.get("bo")).doubleValue(),
                    ((Number) inputMap.get("calcDholeDensityOil")).doubleValue(),
                    ((Number) inputMap.get("downholeDensityWater")).doubleValue(),
                    ((Number) inputMap.get("surfacePull")).doubleValue(),
                    ((Number) inputMap.get("pressControlFriction")).doubleValue(),
                    ((Number) inputMap.get("tubingId")).doubleValue(),
                    ((Number) inputMap.get("lineFrictionFactor")).doubleValue(),
                    ((Number) inputMap.get("tubingDepth")).doubleValue(),
                    ((Number) inputMap.get("casingId")).doubleValue(),
                    ((Number) inputMap.get("toolstringWt")).doubleValue(),
                    ((Number) inputMap.get("toolOd")).doubleValue(),
                    ((Number) inputMap.get("toolstringFriction")).doubleValue()
            );

            // Perform calculation and save to database
            Calculation calculation = calculationService.saveCalculation(input, user, name, description);

            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("id", calculation.getId());
            response.put("name", calculation.getName());
            response.put("description", calculation.getDescription());
            response.put("createdAt", calculation.getCreatedAt());

            try {
                response.put("input", calculation.getInputAsObject());
                response.put("result", calculation.getResultAsObject());
            } catch (IOException e) {
                return ResponseEntity.badRequest().body("Error parsing calculation data");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody CalculationInput input) {
        try {
            CalculationResult result = calculationService.performCalculation(input);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Calculation error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCalculation(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            Calculation calculation = calculationService.getCalculationById(id, user);

            // Delete the calculation
            calculationService.deleteCalculation(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Calculation deleted successfully");
            response.put("id", id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}