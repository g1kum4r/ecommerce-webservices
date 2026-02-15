-- Create countries table
CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    region_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    iso2 VARCHAR(2) NOT NULL UNIQUE,
    iso3 VARCHAR(3) NOT NULL UNIQUE,
    phone_code VARCHAR(20),
    language VARCHAR(100),
    language_code VARCHAR(10),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_countries_region_id ON countries(region_id);
CREATE INDEX idx_countries_iso2 ON countries(iso2);
CREATE INDEX idx_countries_iso3 ON countries(iso3);
CREATE INDEX idx_countries_is_active ON countries(is_active);
CREATE INDEX idx_countries_name ON countries(name);
