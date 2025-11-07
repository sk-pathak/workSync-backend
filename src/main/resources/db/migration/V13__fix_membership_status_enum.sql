-- Convert membership_status from PostgreSQL enum to VARCHAR with CHECK constraint

ALTER TABLE project_members 
ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

ALTER TABLE project_members 
ALTER COLUMN status SET DEFAULT 'APPROVED';

ALTER TABLE project_members 
ADD CONSTRAINT check_membership_status 
CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));

DROP TYPE IF EXISTS membership_status;

DROP TYPE IF EXISTS project_status;
DROP TYPE IF EXISTS task_status;
DROP TYPE IF EXISTS notification_type;
DROP TYPE IF EXISTS notification_status;
