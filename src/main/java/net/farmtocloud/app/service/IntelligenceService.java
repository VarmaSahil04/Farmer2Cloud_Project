package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.entity.DemandData;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.repository.DemandDataRepository;
import net.farmtocloud.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IntelligenceService {

    @Autowired
    private DemandDataRepository demandDataRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Smart Price Engine: Compare farmer price vs market price vs suggested fair price
     */
    public Map<String, Object> getPriceComparison(double farmerPrice, double marketPrice) {
        double suggestedFairPrice = (farmerPrice + marketPrice) / 2.0;
        String verdict;
        if (farmerPrice < suggestedFairPrice * 0.85) {
            verdict = "UNDERPRICED - Farmer should increase price";
        } else if (farmerPrice > suggestedFairPrice * 1.15) {
            verdict = "OVERPRICED - Consider reducing for more orders";
        } else {
            verdict = "FAIR PRICE - Good balance for both parties";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("farmerPrice", farmerPrice);
        result.put("marketPrice", marketPrice);
        result.put("suggestedFairPrice", Math.round(suggestedFairPrice * 100.0) / 100.0);
        result.put("verdict", verdict);
        return result;
    }

    /**
     * Demand Heatmap: Returns all crops with demand levels
     */
    public List<DemandData> getDemandHeatmap() {
        return demandDataRepository.findAllByOrderByOrderCountDesc();
    }

    /**
     * Get demand level for a specific crop
     */
    public String getDemandLevelForCrop(String cropName) {
        return demandDataRepository.findByCropName(cropName.toLowerCase())
                .map(DemandData::getDemandLevel)
                .orElse("MEDIUM");
    }

    /**
     * Record an order and update demand data
     */
    public void recordOrder(String cropName, String region, double price) {
        String normalizedCrop = cropName.toLowerCase();
        DemandData data = demandDataRepository.findByCropName(normalizedCrop)
                .orElse(DemandData.builder()
                        .cropName(normalizedCrop)
                        .region(region != null ? region : "Unknown")
                        .orderCount(0)
                        .avgPrice(0.0)
                        .build());

        data.setOrderCount(data.getOrderCount() + 1);
        data.setAvgPrice(Math.round(((data.getAvgPrice() * (data.getOrderCount() - 1) + price) / data.getOrderCount()) * 100.0) / 100.0);
        data.setRegion(region != null ? region : data.getRegion());

        // Update demand level based on order count
        if (data.getOrderCount() >= 20) {
            data.setDemandLevel("HIGH");
        } else if (data.getOrderCount() >= 5) {
            data.setDemandLevel("MEDIUM");
        } else {
            data.setDemandLevel("LOW");
        }

        data.setUpdatedAt(LocalDateTime.now());
        demandDataRepository.save(data);
        log.info("Demand data updated for {}: level={}, orders={}", normalizedCrop, data.getDemandLevel(), data.getOrderCount());
    }

    /**
     * Smart Recommendations for kitchens based on past orders
     */
    public List<Map<String, Object>> getRecommendations(String kitchenId) {
        List<Order> pastOrders = orderRepository.findByKitchenId(kitchenId);

        // Get frequently ordered crops
        Map<String, Long> cropFrequency = pastOrders.stream()
                .collect(Collectors.groupingBy(Order::getCropName, Collectors.counting()));

        // Get high-demand crops
        List<DemandData> highDemand = demandDataRepository.findByDemandLevel("HIGH");

        List<Map<String, Object>> recommendations = new ArrayList<>();

        // Add frequently ordered crops
        cropFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    Map<String, Object> rec = new LinkedHashMap<>();
                    rec.put("cropName", entry.getKey());
                    rec.put("reason", "Frequently ordered (" + entry.getValue() + " times)");
                    rec.put("type", "REORDER");
                    recommendations.add(rec);
                });

        // Add trending high-demand crops
        highDemand.stream()
                .filter(d -> !cropFrequency.containsKey(d.getCropName()))
                .limit(3)
                .forEach(d -> {
                    Map<String, Object> rec = new LinkedHashMap<>();
                    rec.put("cropName", d.getCropName());
                    rec.put("reason", "Trending - " + d.getOrderCount() + " orders recently");
                    rec.put("type", "TRENDING");
                    rec.put("avgPrice", d.getAvgPrice());
                    recommendations.add(rec);
                });

        return recommendations;
    }
}
