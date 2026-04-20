package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.service.IntelligenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceController {

    @Autowired
    private IntelligenceService intelligenceService;

    @GetMapping("/price-compare")
    public ResponseEntity<ApiResponse> priceComparison(@RequestParam double farmerPrice,
                                                        @RequestParam double marketPrice) {
        return ResponseEntity.ok(ApiResponse.success("Price comparison",
                intelligenceService.getPriceComparison(farmerPrice, marketPrice)));
    }

    @GetMapping("/demand-heatmap")
    public ResponseEntity<ApiResponse> getDemandHeatmap() {
        return ResponseEntity.ok(ApiResponse.success("Demand heatmap",
                intelligenceService.getDemandHeatmap()));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse> getRecommendations(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Recommendations",
                intelligenceService.getRecommendations(auth.getName())));
    }
}
