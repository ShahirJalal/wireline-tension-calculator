package com.oilcalc.backend.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.IOException;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calculation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "input_data", columnDefinition = "text")
    private String inputData;

    @Column(name = "result_data", columnDefinition = "text")
    private String resultData;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    public void setInputFromObject(CalculationInput input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.inputData = mapper.writeValueAsString(input);
    }

    public CalculationInput getInputAsObject() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputData, CalculationInput.class);
    }

    public void setResultFromObject(CalculationResult result) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.resultData = mapper.writeValueAsString(result);
    }

    public CalculationResult getResultAsObject() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(resultData, CalculationResult.class);
    }
}