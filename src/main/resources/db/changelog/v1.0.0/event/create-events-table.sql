-- JSONB is the best optimized type for JSON in PostgreSQL:
-- 1. Stores binary format (faster processing than JSON text)
-- 2. Supports indexing with GIN indexes
-- 3. Efficient querying with operators like @>, ->, ->>, etc.
-- 4. Better compression and storage efficiency

CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(50) NOT NULL,
    type VARCHAR(100) NOT NULL,
    body JSONB,
    metadata JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Standard indexes
CREATE INDEX idx_events_category ON events(category);
CREATE INDEX idx_events_type ON events(type);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_created_at ON events(created_at);
CREATE INDEX idx_events_category_status ON events(category, status);

-- GIN indexes for JSONB columns (allows efficient queries on JSON fields)
CREATE INDEX idx_events_body_gin ON events USING GIN (body);
CREATE INDEX idx_events_metadata_gin ON events USING GIN (metadata);

-- Partial index for querying by userId in body (common query pattern)
CREATE INDEX idx_events_body_user_id ON events ((body->>'userId')) WHERE body->>'userId' IS NOT NULL;