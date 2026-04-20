package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.DeliveryAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends MongoRepository<DeliveryAssignment, String> {
    Optional<DeliveryAssignment> findByOrderId(String orderId);
    List<DeliveryAssignment> findByStatus(String status);
}
