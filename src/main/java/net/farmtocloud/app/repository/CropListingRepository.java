package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.CropListing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropListingRepository extends MongoRepository<CropListing, String> {
    List<CropListing> findByFarmerId(String farmerId);
    List<CropListing> findByStatus(String status);
    List<CropListing> findByCropNameContainingIgnoreCase(String cropName);
    List<CropListing> findByPricePerKgBetween(Double minPrice, Double maxPrice);
    List<CropListing> findByFarmerLocationContainingIgnoreCase(String location);
    List<CropListing> findByStatusAndCropNameContainingIgnoreCase(String status, String cropName);
}
