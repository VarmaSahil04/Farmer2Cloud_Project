package net.farmtocloud.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import net.farmtocloud.app.entity.Order;
import net.farmtocloud.app.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends transactional emails for account and order events via Brevo's HTTPS API,
 * using the JDK's built-in HttpClient — no third-party mail SDK dependency needed.
 *
 * Brevo (over HTTPS, port 443) is used instead of raw SMTP because most free-tier
 * hosts (Render, Railway, etc.) block outbound SMTP ports (25/465/587) to prevent
 * spam abuse. HTTPS is never blocked, since it's the same port normal web traffic uses.
 *
 * All sends are async and swallow their own exceptions — a bad API key or a
 * Brevo outage should never block or fail the request that triggered the
 * email (signup, order status update, etc.).
 */
@Service
@Slf4j
public class EmailService {

    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.brevo.api-key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

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
            log.warn("Skipping email send to {} — app.mail.from is not configured.", to);
            return;
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Skipping email send to {} — app.brevo.api-key is not configured. " +
                    "Set the BREVO_API_KEY environment variable.", to);
            return;
        }

        try {
            String jsonPayload = buildBrevoPayload(to, subject, body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_ENDPOINT))
                    .timeout(REQUEST_TIMEOUT)
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Brevo returns 201 Created on success, with the message ID in the body.
            if (response.statusCode() == 200 || response.statusCode() == 201 || response.statusCode() == 202) {
                log.info("Email sent to {} — subject: {} (status {})", to, subject, response.statusCode());
            } else {
                // Never let email failures break the calling business flow
                log.error("Brevo rejected email to {} — subject: {}. Status: {}, Body: {}",
                        to, subject, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {} — subject: {}. Reason: {}", to, subject, e.getMessage());
        }
    }

    /**
     * Builds the JSON body for Brevo's /v3/smtp/email endpoint using Jackson,
     * so subject/body text is always correctly JSON-escaped regardless of content
     * (quotes, backslashes, newlines, unicode) — no manual string escaping needed.
     */
    private String buildBrevoPayload(String to, String subject, String body) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode sender = objectMapper.createObjectNode();
        sender.put("name", fromName);
        sender.put("email", fromAddress);
        root.set("sender", sender);

        ArrayNode toArray = objectMapper.createArrayNode();
        ObjectNode toEntry = objectMapper.createObjectNode();
        toEntry.put("email", to);
        toArray.add(toEntry);
        root.set("to", toArray);

        root.put("subject", subject);
        // Sending as plain text wrapped in <pre> so line breaks render correctly as HTML content.
        root.put("htmlContent", "<pre style=\"font-family:inherit; white-space:pre-wrap;\">" + escapeHtml(body) + "</pre>");

        return objectMapper.writeValueAsString(root);
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
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