package net.farmtocloud.app.controller;

import net.farmtocloud.app.dto.ApiResponse;
import net.farmtocloud.app.entity.Notification;
import net.farmtocloud.app.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse> getUnreadNotifications(Authentication auth) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Unread notifications", notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable String id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }
}
