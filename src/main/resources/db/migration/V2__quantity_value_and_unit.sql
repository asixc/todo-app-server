ALTER TABLE items
    ADD COLUMN IF NOT EXISTS quantity_value NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS quantity_unit VARCHAR(16);

UPDATE items
SET quantity_value = quantity::numeric,
    quantity_unit = 'unit'
WHERE quantity IS NOT NULL
  AND quantity_value IS NULL;

ALTER TABLE items ALTER COLUMN quantity DROP DEFAULT;
ALTER TABLE items DROP COLUMN IF EXISTS quantity;
