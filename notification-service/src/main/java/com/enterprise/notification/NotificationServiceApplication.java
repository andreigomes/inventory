package com.enterprise.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for Notification Service.
 * Handles real-time notifications for inventory events.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.enterprise.notification",
    "com.enterprise.shared"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
