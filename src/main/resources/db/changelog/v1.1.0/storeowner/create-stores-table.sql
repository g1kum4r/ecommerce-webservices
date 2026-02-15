-- Create stores table
CREATE TABLE stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_owner_profile_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    store_category_id BIGINT,
    location_type VARCHAR(50) NOT NULL CHECK (location_type IN ('ONLINE', 'PHYSICAL', 'BOTH')),
    -- Physical location details (nullable for online-only stores)
    address_id UUID,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    -- Contact information
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(500),
    -- Business hours (JSON or text format)
    business_hours TEXT,
    -- Store status
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_owner_profile_id) REFERENCES store_owner_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (store_category_id) REFERENCES store_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE SET NULL
);

-- Create indexes for faster queries
CREATE INDEX idx_stores_store_owner_profile_id ON stores(store_owner_profile_id);
CREATE INDEX idx_stores_slug ON stores(slug);
CREATE INDEX idx_stores_location_type ON stores(location_type);
CREATE INDEX idx_stores_is_active ON stores(is_active);
CREATE INDEX idx_stores_store_category_id ON stores(store_category_id);
-- Create index for faster lookups
CREATE INDEX idx_stores_address_id ON stores(address_id);
