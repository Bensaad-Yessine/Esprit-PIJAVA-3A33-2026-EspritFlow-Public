-- ═══════════════════════════════════════════════════════════
--  Migration: Add last_login column for streak engagement
--  Run once against the `pidev` database
-- ═══════════════════════════════════════════════════════════

USE pidev;

-- Add last_login column (nullable DATETIME, defaults to NULL)
ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `last_login` DATETIME NULL DEFAULT NULL
        COMMENT 'Timestamp of the last successful login — used by StreakEngagementService';

-- Optional: index for fast queries on last_login
CREATE INDEX IF NOT EXISTS idx_user_last_login
    ON `user` (`last_login`);

-- Verify
SELECT COUNT(*) AS users_with_last_login
FROM `user`
WHERE last_login IS NOT NULL;
