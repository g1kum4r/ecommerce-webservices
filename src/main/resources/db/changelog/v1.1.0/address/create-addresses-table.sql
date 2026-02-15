-- Create addresses table
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id BIGINT NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    street_no VARCHAR(50),
    area VARCHAR(100),
    division VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE RESTRICT
);

-- Create indexes for faster lookups
CREATE INDEX idx_addresses_city_id ON addresses(city_id);
CREATE INDEX idx_addresses_coordinates ON addresses(latitude, longitude);
