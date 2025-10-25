-- Migration to convert priority from integer to enum string values

ALTER TABLE tasks
    ADD COLUMN priority_temp VARCHAR(20);

-- Map: 1 -> CRITICAL, 2 -> HIGH, 3 -> MEDIUM, 4 -> LOW, 5 -> LOW
UPDATE tasks
SET priority_temp = CASE
    WHEN priority = 1 THEN 'CRITICAL'
    WHEN priority = 2 THEN 'HIGH'
    WHEN priority = 3 THEN 'MEDIUM'
    WHEN priority = 4 THEN 'LOW'
    WHEN priority = 5 THEN 'LOW'
    ELSE NULL
END
WHERE priority IS NOT NULL;

ALTER TABLE tasks
    DROP COLUMN priority;

ALTER TABLE tasks
    RENAME COLUMN priority_temp TO priority;

ALTER TABLE tasks
    ADD CONSTRAINT check_priority_enum
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));
