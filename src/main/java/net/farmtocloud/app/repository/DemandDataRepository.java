package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.DemandData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandDataRepository extends MongoRepository<DemandData, String> {
    Optional<DemandData> findByCropName(String cropName);
    List<DemandData> findByDemandLevel(String demandLevel);
    List<DemandData> findAllByOrderByOrderCountDesc();
    List<DemandData> findByRegion(String region);
}
