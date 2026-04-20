package net.farmtocloud.app.repository;

import net.farmtocloud.app.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdAndIsReadFalse(String userId);
}
