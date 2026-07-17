package com.oilcalc.backend.repository;

import com.oilcalc.backend.model.Calculation;
import com.oilcalc.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationRepository extends JpaRepository<Calculation, Long> {
    List<Calculation> findByUserOrderByCreatedAtDesc(User user);
}