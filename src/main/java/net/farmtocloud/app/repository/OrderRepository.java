package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByKitchenId(String kitchenId);
    List<Order> findByFarmerId(String farmerId);
    List<Order> findByStatus(String status);
    List<Order> findByStatusIn(List<String> statuses);
    List<Order> findByKitchenIdAndStatus(String kitchenId, String status);
    List<Order> findByFarmerIdAndStatus(String farmerId, String status);
    List<Order> findByCropName(String cropName);
    long countByKitchenId(String kitchenId);
    long countByFarmerId(String farmerId);
}