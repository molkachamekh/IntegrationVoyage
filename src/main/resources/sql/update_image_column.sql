-- Script to update the image column in the voyage table to MEDIUMTEXT
-- This will allow storing larger images (up to 16MB)
ALTER TABLE voyage
MODIFY COLUMN image MEDIUMTEXT;
-- If you need support for even larger images (up to 4GB), use LONGTEXT instead:
-- ALTER TABLE voyage MODIFY COLUMN image LONGTEXT;
-- Note: You can run this script directly in your MySQL client like MySQL Workbench
-- or through a command line client.