-- Create states table
CREATE TABLE states (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    state_code VARCHAR(10),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_states_country_id ON states(country_id);
CREATE INDEX idx_states_state_code ON states(state_code);
CREATE INDEX idx_states_is_active ON states(is_active);
CREATE INDEX idx_states_name ON states(name);
