-- Create cities table
CREATE TABLE cities (
    id BIGSERIAL PRIMARY KEY,
    state_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (state_id) REFERENCES states(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_cities_state_id ON cities(state_id);
CREATE INDEX idx_cities_is_active ON cities(is_active);
CREATE INDEX idx_cities_name ON cities(name);
CREATE INDEX idx_cities_coordinates ON cities(latitude, longitude);
