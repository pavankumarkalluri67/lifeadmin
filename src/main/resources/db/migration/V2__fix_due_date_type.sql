-- V2__fix_due_date_type.sql
-- Fix schema mismatch: due_date column should be DATE, not TIMESTAMP

ALTER TABLE obligations
    ALTER COLUMN due_date TYPE DATE USING due_date::date;
