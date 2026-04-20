package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.entity.User;
import net.farmtocloud.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public User updateProfile(String userId, User updatedData) {
        User user = getUserById(userId);

        if (updatedData.getName() != null) user.setName(updatedData.getName());
        if (updatedData.getPhone() != null) user.setPhone(updatedData.getPhone());
        if (updatedData.getLocation() != null) user.setLocation(updatedData.getLocation());
        if (updatedData.getLatitude() != null) user.setLatitude(updatedData.getLatitude());
        if (updatedData.getLongitude() != null) user.setLongitude(updatedData.getLongitude());
        if (updatedData.getCropTypes() != null) user.setCropTypes(updatedData.getCropTypes());
        if (updatedData.getUpiId() != null) user.setUpiId(updatedData.getUpiId());
        if (updatedData.getBusinessName() != null) user.setBusinessName(updatedData.getBusinessName());
        if (updatedData.getAddress() != null) user.setAddress(updatedData.getAddress());
        if (updatedData.getDailyRequirements() != null) user.setDailyRequirements(updatedData.getDailyRequirements());

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<User> getFarmers() {
        return userRepository.findByRole("FARMER");
    }

    public List<User> getKitchens() {
        return userRepository.findByRole("KITCHEN");
    }

    public void updateTrustScore(String farmerId, boolean successfulDelivery) {
        User farmer = getUserById(farmerId);
        farmer.setTotalOrders(farmer.getTotalOrders() + 1);
        if (successfulDelivery) {
            farmer.setSuccessfulDeliveries(farmer.getSuccessfulDeliveries() + 1);
        }
        double successRate = (double) farmer.getSuccessfulDeliveries() / farmer.getTotalOrders() * 100;
        farmer.setDeliverySuccessRate(Math.round(successRate * 100.0) / 100.0);

        // Trust score = weighted average of delivery success rate
        double trustScore = Math.min(5.0, (successRate / 20.0));
        farmer.setTrustScore(Math.round(trustScore * 10.0) / 10.0);

        if (farmer.getTotalOrders() >= 5 && farmer.getDeliverySuccessRate() >= 80) {
            farmer.setVerified(true);
        }

        farmer.setUpdatedAt(LocalDateTime.now());
        userRepository.save(farmer);
        log.info("Updated trust score for farmer {}: {}", farmerId, farmer.getTrustScore());
    }
}
