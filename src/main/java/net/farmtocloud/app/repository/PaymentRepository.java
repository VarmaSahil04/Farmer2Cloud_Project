package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByFarmerId(String farmerId);
    List<Payment> findByKitchenId(String kitchenId);
    List<Payment> findByStatus(String status);
}
