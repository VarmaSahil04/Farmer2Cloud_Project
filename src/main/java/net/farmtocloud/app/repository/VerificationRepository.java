package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.Verification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends MongoRepository<Verification, String> {
    Optional<Verification> findByOrderId(String orderId);
}
