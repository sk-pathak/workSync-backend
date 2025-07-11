ALTER TABLE notifications 
ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

ALTER TABLE notifications 
ALTER COLUMN status SET DEFAULT 'PENDING';

ALTER TABLE notifications 
ADD CONSTRAINT check_notification_status 
CHECK (status IN ('PENDING', 'READ', 'DISMISSED')); 