package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.Dispute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends MongoRepository<Dispute, String> {
    List<Dispute> findByOrderId(String orderId);
    List<Dispute> findByRaisedBy(String userId);
    List<Dispute> findByStatus(String status);
}
