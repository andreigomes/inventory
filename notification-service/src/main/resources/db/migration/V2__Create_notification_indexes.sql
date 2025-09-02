-- Notification Service Database Indexes
-- V2__Create_notification_indexes.sql

SET search_path TO notification, public;

-- Indexes for notification_templates table
CREATE INDEX idx_notification_templates_type ON notification_templates(type);
CREATE INDEX idx_notification_templates_active ON notification_templates(is_active);
CREATE INDEX idx_notification_templates_name ON notification_templates(name);

-- Indexes for notification_channels table
CREATE INDEX idx_notification_channels_type ON notification_channels(type);
CREATE INDEX idx_notification_channels_active ON notification_channels(is_active);

-- Indexes for notification_subscriptions table
CREATE INDEX idx_notification_subscriptions_user ON notification_subscriptions(user_id);
CREATE INDEX idx_notification_subscriptions_event ON notification_subscriptions(event_type);
CREATE INDEX idx_notification_subscriptions_channel ON notification_subscriptions(channel_id);
CREATE INDEX idx_notification_subscriptions_active ON notification_subscriptions(is_active);
CREATE INDEX idx_notification_subscriptions_user_event ON notification_subscriptions(user_id, event_type);

-- Indexes for notifications table
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_priority ON notifications(priority);
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_at);
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_event_type ON notifications(event_type);
CREATE INDEX idx_notifications_event_id ON notifications(event_id);
CREATE INDEX idx_notifications_channel ON notifications(channel_id);
CREATE INDEX idx_notifications_template ON notifications(template_id);
CREATE INDEX idx_notifications_retry ON notifications(retry_count, status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Indexes for notification_events table
CREATE INDEX idx_notification_events_notification ON notification_events(notification_id);
CREATE INDEX idx_notification_events_type ON notification_events(event_type);
CREATE INDEX idx_notification_events_occurred ON notification_events(occurred_at);

-- Indexes for notification_metrics table
CREATE INDEX idx_notification_metrics_date ON notification_metrics(date);
CREATE INDEX idx_notification_metrics_channel_type ON notification_metrics(channel_type);
CREATE INDEX idx_notification_metrics_event_type ON notification_metrics(event_type);

-- Composite indexes for common queries
CREATE INDEX idx_notifications_status_scheduled ON notifications(status, scheduled_at);
CREATE INDEX idx_notifications_recipient_status ON notifications(recipient, status);
CREATE INDEX idx_notifications_event_status ON notifications(event_type, status);
CREATE INDEX idx_notification_metrics_date_channel ON notification_metrics(date, channel_type);
