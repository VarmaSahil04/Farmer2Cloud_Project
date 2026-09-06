package net.farmtocloud.app.service;

import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails for account and order events.
 *
 * All sends are async and swallow their own exceptions — a broken SMTP
 * connection or bad credentials should never block or fail the request
 * that triggered the email (signup, order status update, etc.).
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to Farm To Cloud Kitchen, " + user.getName() + "!";
        String body = buildWelcomeBody(user);
        send(user.getEmail(), subject, body);
    }

    @Async
    public void sendOrderStatusEmail(Order order, String recipientEmail, String recipientName) {
        String action = "PENDING".equals(order.getStatus()) ? "Order Confirmation" : "Order Update";
        String subject = action + ": " + order.getCropName() + " — " + humanize(order.getStatus());
        String body = buildOrderStatusBody(order, recipientName);
        send(recipientEmail, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Reset your Farm To Cloud Kitchen password";
        String body = buildPasswordResetBody(user, resetToken);
        send(user.getEmail(), subject, body);
    }

    private void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("Skipping email send — recipient address is empty. Subject: {}", subject);
            return;
        }
        if (fromAddress == null || fromAddress.isBlank()) {
            log.warn("Skipping email send to {} — app.mail.from is not configured. " +
                    "Set GMAIL_USERNAME and GMAIL_APP_PASSWORD environment variables.", to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception e) {
            // Never let email failures break the calling business flow (signup, order update, etc.)
            log.error("Failed to send email to {} — subject: {}. Reason: {}", to, subject, e.getMessage());
        }
    }

    private String buildWelcomeBody(User user) {
        return "Hi " + user.getName() + ",\n\n"
                + "Welcome to Farm To Cloud Kitchen! Your account has been created as a "
                + user.getRole() + ".\n\n"
                + (("FARMER".equalsIgnoreCase(user.getRole()))
                    ? "You can now list your crops and start receiving orders from cloud kitchens directly.\n\n"
                    : "You can now browse crop listings and start ordering fresh produce directly from farmers.\n\n")
                + "Happy trading,\n"
                + fromName;
    }

    private String buildOrderStatusBody(Order order, String recipientName) {
        return "Hi " + recipientName + ",\n\n"
                + "Your order for " + order.getQuantity() + " kg of " + order.getCropName()
                + " has been updated.\n\n"
                + "New status: " + humanize(order.getStatus()) + "\n"
                + "Order ID: " + order.getId() + "\n"
                + "Total: ₹" + order.getTotalPrice() + "\n\n"
                + "You can check full details by logging into your dashboard.\n\n"
                + "Thanks,\n"
                + fromName;
    }

    private String buildPasswordResetBody(User user, String resetToken) {
        return "Hi " + user.getName() + ",\n\n"
                + "We received a request to reset your Farm To Cloud Kitchen password.\n\n"
                + "Your reset code is: " + resetToken + "\n\n"
                + "Use this code with the reset-password screen (or POST /api/auth/reset-password) "
                + "within the next 30 minutes to set a new password.\n\n"
                + "If you didn't request this, you can safely ignore this email — your password "
                + "will remain unchanged.\n\n"
                + "Thanks,\n"
                + fromName;
    }

    private String humanize(String status) {
        if (status == null) {
            return "Updated";
        }
        switch (status) {
            case "PENDING":
                return "Order Placed";
            case "PICKUP_ASSIGNED":
                return "Pickup Assigned";
            case "VERIFIED":
                return "Verified by Delivery Partner";
            case "FARMER_CONFIRMED":
                return "Confirmed by Farmer";
            case "IN_TRANSIT":
                return "In Transit";
            case "DELIVERED":
                return "Delivered";
            case "CANCELLED":
                return "Cancelled";
            default:
                return status;
        }
    }
}
