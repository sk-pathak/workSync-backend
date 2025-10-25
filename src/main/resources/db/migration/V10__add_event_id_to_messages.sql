-- Add event_id column to messages table for Kafka idempotency (ensure deduplication)

ALTER TABLE messages
ADD COLUMN event_id UUID;

ALTER TABLE messages
ADD CONSTRAINT uk_messages_event_id UNIQUE (event_id);

CREATE INDEX idx_messages_event_id ON messages(event_id);