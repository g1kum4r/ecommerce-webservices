-- Seed regions data
INSERT INTO regions (name, code) VALUES
('Africa', 'AF'),
('Asia', 'AS'),
('Europe', 'EU'),
('North America', 'NA'),
('South America', 'SA'),
('Oceania', 'OC'),
('Antarctica', 'AN');

-- Seed countries data with ISO codes and language info
INSERT INTO countries (region_id, name, iso2, iso3, phone_code, language, language_code) VALUES
-- Asia
((SELECT id FROM regions WHERE code = 'AS'), 'Pakistan', 'PK', 'PAK', '+92', 'Urdu', 'ur'),
((SELECT id FROM regions WHERE code = 'AS'), 'India', 'IN', 'IND', '+91', 'Hindi', 'hi'),
((SELECT id FROM regions WHERE code = 'AS'), 'Bangladesh', 'BD', 'BGD', '+880', 'Bengali', 'bn'),
((SELECT id FROM regions WHERE code = 'AS'), 'China', 'CN', 'CHN', '+86', 'Mandarin', 'zh'),
((SELECT id FROM regions WHERE code = 'AS'), 'Japan', 'JP', 'JPN', '+81', 'Japanese', 'ja'),
((SELECT id FROM regions WHERE code = 'AS'), 'South Korea', 'KR', 'KOR', '+82', 'Korean', 'ko'),
((SELECT id FROM regions WHERE code = 'AS'), 'Indonesia', 'ID', 'IDN', '+62', 'Indonesian', 'id'),
((SELECT id FROM regions WHERE code = 'AS'), 'Thailand', 'TH', 'THA', '+66', 'Thai', 'th'),
((SELECT id FROM regions WHERE code = 'AS'), 'Malaysia', 'MY', 'MYS', '+60', 'Malay', 'ms'),
((SELECT id FROM regions WHERE code = 'AS'), 'Singapore', 'SG', 'SGP', '+65', 'English', 'en'),
((SELECT id FROM regions WHERE code = 'AS'), 'Philippines', 'PH', 'PHL', '+63', 'Filipino', 'fil'),
((SELECT id FROM regions WHERE code = 'AS'), 'Vietnam', 'VN', 'VNM', '+84', 'Vietnamese', 'vi'),
((SELECT id FROM regions WHERE code = 'AS'), 'Turkey', 'TR', 'TUR', '+90', 'Turkish', 'tr'),
((SELECT id FROM regions WHERE code = 'AS'), 'Saudi Arabia', 'SA', 'SAU', '+966', 'Arabic', 'ar'),
((SELECT id FROM regions WHERE code = 'AS'), 'United Arab Emirates', 'AE', 'ARE', '+971', 'Arabic', 'ar'),

-- North America
((SELECT id FROM regions WHERE code = 'NA'), 'United States', 'US', 'USA', '+1', 'English', 'en'),
((SELECT id FROM regions WHERE code = 'NA'), 'Canada', 'CA', 'CAN', '+1', 'English', 'en'),
((SELECT id FROM regions WHERE code = 'NA'), 'Mexico', 'MX', 'MEX', '+52', 'Spanish', 'es'),

-- Europe
((SELECT id FROM regions WHERE code = 'EU'), 'United Kingdom', 'GB', 'GBR', '+44', 'English', 'en'),
((SELECT id FROM regions WHERE code = 'EU'), 'Germany', 'DE', 'DEU', '+49', 'German', 'de'),
((SELECT id FROM regions WHERE code = 'EU'), 'France', 'FR', 'FRA', '+33', 'French', 'fr'),
((SELECT id FROM regions WHERE code = 'EU'), 'Italy', 'IT', 'ITA', '+39', 'Italian', 'it'),
((SELECT id FROM regions WHERE code = 'EU'), 'Spain', 'ES', 'ESP', '+34', 'Spanish', 'es'),
((SELECT id FROM regions WHERE code = 'EU'), 'Netherlands', 'NL', 'NLD', '+31', 'Dutch', 'nl'),
((SELECT id FROM regions WHERE code = 'EU'), 'Poland', 'PL', 'POL', '+48', 'Polish', 'pl'),
((SELECT id FROM regions WHERE code = 'EU'), 'Russia', 'RU', 'RUS', '+7', 'Russian', 'ru'),

-- Africa
((SELECT id FROM regions WHERE code = 'AF'), 'Nigeria', 'NG', 'NGA', '+234', 'English', 'en'),
((SELECT id FROM regions WHERE code = 'AF'), 'South Africa', 'ZA', 'ZAF', '+27', 'English', 'en'),
((SELECT id FROM regions WHERE code = 'AF'), 'Egypt', 'EG', 'EGY', '+20', 'Arabic', 'ar'),
((SELECT id FROM regions WHERE code = 'AF'), 'Kenya', 'KE', 'KEN', '+254', 'Swahili', 'sw'),

-- South America
((SELECT id FROM regions WHERE code = 'SA'), 'Brazil', 'BR', 'BRA', '+55', 'Portuguese', 'pt'),
((SELECT id FROM regions WHERE code = 'SA'), 'Argentina', 'AR', 'ARG', '+54', 'Spanish', 'es'),
((SELECT id FROM regions WHERE code = 'SA'), 'Colombia', 'CO', 'COL', '+57', 'Spanish', 'es'),
((SELECT id FROM regions WHERE code = 'SA'), 'Chile', 'CL', 'CHL', '+56', 'Spanish', 'es'),

-- Oceania
((SELECT id FROM regions WHERE code = 'OC'), 'Australia', 'AU', 'AUS', '+61', 'English', 'en'),
((SELECT id FROM regions WHERE code = 'OC'), 'New Zealand', 'NZ', 'NZL', '+64', 'English', 'en');

-- Seed states data for Pakistan
INSERT INTO states (country_id, name, state_code) VALUES
((SELECT id FROM countries WHERE iso2 = 'PK'), 'Punjab', 'PB'),
((SELECT id FROM countries WHERE iso2 = 'PK'), 'Sindh', 'SD'),
((SELECT id FROM countries WHERE iso2 = 'PK'), 'Khyber Pakhtunkhwa', 'KP'),
((SELECT id FROM countries WHERE iso2 = 'PK'), 'Balochistan', 'BA'),
((SELECT id FROM countries WHERE iso2 = 'PK'), 'Islamabad Capital Territory', 'IS'),
((SELECT id FROM countries WHERE iso2 = 'PK'), 'Gilgit-Baltistan', 'GB'),
((SELECT id FROM countries WHERE iso2 = 'PK'), 'Azad Jammu and Kashmir', 'JK');

-- Seed states data for United States (sample states)
INSERT INTO states (country_id, name, state_code) VALUES
((SELECT id FROM countries WHERE iso2 = 'US'), 'California', 'CA'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'Texas', 'TX'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'Florida', 'FL'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'New York', 'NY'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'Illinois', 'IL'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'Pennsylvania', 'PA'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'Ohio', 'OH'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'Georgia', 'GA'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'North Carolina', 'NC'),
((SELECT id FROM countries WHERE iso2 = 'US'), 'Michigan', 'MI');

-- Seed cities data for Punjab, Pakistan
INSERT INTO cities (state_id, name, latitude, longitude) VALUES
((SELECT id FROM states WHERE name = 'Punjab' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Lahore', 31.5204, 74.3587),
((SELECT id FROM states WHERE name = 'Punjab' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Faisalabad', 31.4180, 73.0790),
((SELECT id FROM states WHERE name = 'Punjab' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Rawalpindi', 33.5651, 73.0169),
((SELECT id FROM states WHERE name = 'Punjab' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Multan', 30.1575, 71.5249),
((SELECT id FROM states WHERE name = 'Punjab' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Gujranwala', 32.1617, 74.1883),
((SELECT id FROM states WHERE name = 'Punjab' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Sialkot', 32.4945, 74.5229),
((SELECT id FROM states WHERE name = 'Punjab' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Bahawalpur', 29.3956, 71.6836);

-- Seed cities data for Sindh, Pakistan
INSERT INTO cities (state_id, name, latitude, longitude) VALUES
((SELECT id FROM states WHERE name = 'Sindh' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Karachi', 24.8607, 67.0011),
((SELECT id FROM states WHERE name = 'Sindh' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Hyderabad', 25.3960, 68.3578),
((SELECT id FROM states WHERE name = 'Sindh' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Sukkur', 27.7058, 68.8574),
((SELECT id FROM states WHERE name = 'Sindh' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Larkana', 27.5590, 68.2120);

-- Seed cities data for Islamabad Capital Territory
INSERT INTO cities (state_id, name, latitude, longitude) VALUES
((SELECT id FROM states WHERE name = 'Islamabad Capital Territory' AND country_id = (SELECT id FROM countries WHERE iso2 = 'PK')), 'Islamabad', 33.6844, 73.0479);

-- Seed cities data for California, USA
INSERT INTO cities (state_id, name, latitude, longitude) VALUES
((SELECT id FROM states WHERE name = 'California' AND country_id = (SELECT id FROM countries WHERE iso2 = 'US')), 'Los Angeles', 34.0522, -118.2437),
((SELECT id FROM states WHERE name = 'California' AND country_id = (SELECT id FROM countries WHERE iso2 = 'US')), 'San Francisco', 37.7749, -122.4194),
((SELECT id FROM states WHERE name = 'California' AND country_id = (SELECT id FROM countries WHERE iso2 = 'US')), 'San Diego', 32.7157, -117.1611),
((SELECT id FROM states WHERE name = 'California' AND country_id = (SELECT id FROM countries WHERE iso2 = 'US')), 'San Jose', 37.3382, -121.8863);

-- Seed cities data for New York, USA
INSERT INTO cities (state_id, name, latitude, longitude) VALUES
((SELECT id FROM states WHERE name = 'New York' AND country_id = (SELECT id FROM countries WHERE iso2 = 'US')), 'New York City', 40.7128, -74.0060),
((SELECT id FROM states WHERE name = 'New York' AND country_id = (SELECT id FROM countries WHERE iso2 = 'US')), 'Buffalo', 42.8864, -78.8784),
((SELECT id FROM states WHERE name = 'New York' AND country_id = (SELECT id FROM countries WHERE iso2 = 'US')), 'Rochester', 43.1566, -77.6088);
