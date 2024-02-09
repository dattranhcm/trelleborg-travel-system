package com.travel.system.trelleborg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class BusFeeService {

    @Value("#{${bus.fee}}")
    private Map<String, BigDecimal> costs;

    @PostConstruct
    public void initialize() {
        addReversedPairs(costs);
    }

    public Map<String, BigDecimal> getBusFees() {
        return costs;
    }

    public Map<String, BigDecimal> filterKeys(String stopStation) {
        Map<String, BigDecimal> filteredMap = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : costs.entrySet()) {
            String key = entry.getKey();
            if (key.contains(stopStation)) {
                filteredMap.put(key, entry.getValue());
            }
        }
        return filteredMap;
    }

    public BigDecimal findHighestValue(Map<String, BigDecimal> busFees) {
        BigDecimal highestValue = BigDecimal.ZERO;
        for (BigDecimal value : busFees.values()) {
            if (value.compareTo(highestValue) > 0) {
                highestValue = value;
            }
        }
        return highestValue;
    }
    private void addReversedPairs(Map<String, BigDecimal> busFees) {
        Map<String, BigDecimal> resultMap = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : busFees.entrySet()) {
            String key = entry.getKey();
            BigDecimal value = entry.getValue();
            String reversedKey = reverseKey(key);
            resultMap.put(reversedKey, value);
        }
        busFees.putAll(resultMap);
    }

    private String reverseKey(String key) {
        String[] parts = key.split("_TO_");
        if (parts.length == 2) {
            return parts[1] + "_TO_" + parts[0];
        }
        return key;
    }

}
