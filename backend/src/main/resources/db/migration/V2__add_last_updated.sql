ALTER TABLE work_order
    ADD COLUMN last_updated TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX idx_work_order_last_updated ON work_order (last_updated DESC);
