-- Create membership_status enum type
CREATE TYPE membership_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- Add status column to project_members table
ALTER TABLE project_members ADD COLUMN status membership_status NOT NULL DEFAULT 'APPROVED';

-- Update existing records to be APPROVED (backward compatibility)
UPDATE project_members SET status = 'APPROVED' WHERE status IS NULL;
