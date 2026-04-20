package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.dto.CropListingRequest;
import net.farmtocloud.app.entity.CropListing;
import net.farmtocloud.app.service.CropListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropListingController {

    @Autowired
    private CropListingService cropListingService;

    @PostMapping
    public ResponseEntity<ApiResponse> createListing(Authentication auth,
                                                      @Valid @RequestBody CropListingRequest request) {
        try {
            CropListing listing = cropListingService.createListing(auth.getName(), request);
            return ResponseEntity.ok(ApiResponse.success("Crop listing created", listing));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllListings() {
        List<CropListing> listings = cropListingService.getAvailableListings();
        return ResponseEntity.ok(ApiResponse.success("Available listings", listings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getListingById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Listing details",
                    cropListingService.getListingById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/farmer")
    public ResponseEntity<ApiResponse> getMyListings(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("My listings",
                cropListingService.getListingsByFarmer(auth.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateListing(@PathVariable String id,
                                                      @Valid @RequestBody CropListingRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Listing updated",
                    cropListingService.updateListing(id, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteListing(@PathVariable String id) {
        cropListingService.deleteListing(id);
        return ResponseEntity.ok(ApiResponse.success("Listing deleted"));
    }
}
