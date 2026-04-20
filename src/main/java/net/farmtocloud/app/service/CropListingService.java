package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.dto.CropListingRequest;
import net.farmtocloud.app.entity.CropListing;
import net.farmtocloud.app.entity.User;
import net.farmtocloud.app.repository.CropListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CropListingService {

    @Autowired
    private CropListingRepository cropListingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private IntelligenceService intelligenceService;

    public CropListing createListing(String farmerId, CropListingRequest request) {
        User farmer = userService.getUserById(farmerId);

        double marketPrice = request.getMarketPrice() != null ? request.getMarketPrice() : request.getPricePerKg() * 1.1;
        double suggestedFairPrice = (request.getPricePerKg() + marketPrice) / 2.0;

        CropListing listing = CropListing.builder()
                .farmerId(farmerId)
                .farmerName(farmer.getName())
                .farmerLocation(farmer.getLocation())
                .cropName(request.getCropName())
                .quantity(request.getQuantity())
                .pricePerKg(request.getPricePerKg())
                .marketPrice(Math.round(marketPrice * 100.0) / 100.0)
                .suggestedFairPrice(Math.round(suggestedFairPrice * 100.0) / 100.0)
                .availableDate(request.getAvailableDate())
                .imageUrl(request.getImageUrl())
                .demandLevel(intelligenceService.getDemandLevelForCrop(request.getCropName()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CropListing saved = cropListingRepository.save(listing);
        log.info("Crop listing created: {} by farmer {}", saved.getCropName(), farmerId);
        return saved;
    }

    public CropListing updateListing(String listingId, CropListingRequest request) {
        CropListing listing = getListingById(listingId);

        if (request.getCropName() != null) listing.setCropName(request.getCropName());
        if (request.getQuantity() != null) listing.setQuantity(request.getQuantity());
        if (request.getPricePerKg() != null) {
            listing.setPricePerKg(request.getPricePerKg());
            double mp = request.getMarketPrice() != null ? request.getMarketPrice() : request.getPricePerKg() * 1.1;
            listing.setMarketPrice(Math.round(mp * 100.0) / 100.0);
            listing.setSuggestedFairPrice(Math.round(((request.getPricePerKg() + mp) / 2.0) * 100.0) / 100.0);
        }
        if (request.getAvailableDate() != null) listing.setAvailableDate(request.getAvailableDate());
        if (request.getImageUrl() != null) listing.setImageUrl(request.getImageUrl());

        listing.setUpdatedAt(LocalDateTime.now());
        return cropListingRepository.save(listing);
    }

    public CropListing getListingById(String id) {
        return cropListingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop listing not found: " + id));
    }

    public List<CropListing> getListingsByFarmer(String farmerId) {
        return cropListingRepository.findByFarmerId(farmerId);
    }

    public List<CropListing> getAvailableListings() {
        return cropListingRepository.findByStatus("AVAILABLE");
    }

    public List<CropListing> searchListings(String query) {
        return cropListingRepository.findByCropNameContainingIgnoreCase(query);
    }

    public List<CropListing> filterByPriceRange(Double min, Double max) {
        return cropListingRepository.findByPricePerKgBetween(min, max);
    }

    public List<CropListing> filterByLocation(String location) {
        return cropListingRepository.findByFarmerLocationContainingIgnoreCase(location);
    }

    public void deleteListing(String id) {
        cropListingRepository.deleteById(id);
        log.info("Crop listing deleted: {}", id);
    }
}
