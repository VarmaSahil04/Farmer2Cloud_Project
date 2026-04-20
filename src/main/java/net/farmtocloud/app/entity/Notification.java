package net.farmtocloud.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;
    
    private String userId;
    private String message;
    private String type;
    
    @Builder.Default
    private boolean isRead = false;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
