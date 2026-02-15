# Address System Implementation

## Overview
Comprehensive hierarchical address system implementation with support for regions, countries (with ISO codes), states, cities, and addresses. The system provides public APIs for querying address data with pagination and search capabilities.

## Database Schema

### Tables Created

#### 1. **regions**
- `id` BIGSERIAL PRIMARY KEY
- `name` VARCHAR(100) NOT NULL UNIQUE
- `code` VARCHAR(10) NOT NULL UNIQUE
- `is_active` BOOLEAN DEFAULT true
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

**Indexes:**
- `idx_regions_code` on code
- `idx_regions_is_active` on is_active

#### 2. **countries**
- `id` BIGSERIAL PRIMARY KEY
- `region_id` BIGINT NOT NULL (FK to regions)
- `name` VARCHAR(100) NOT NULL
- `iso2` VARCHAR(2) NOT NULL UNIQUE (ISO 3166-1 alpha-2)
- `iso3` VARCHAR(3) NOT NULL UNIQUE (ISO 3166-1 alpha-3)
- `phone_code` VARCHAR(20) (e.g., +92, +1)
- `language` VARCHAR(100) (primary language)
- `language_code` VARCHAR(10) (ISO 639-1)
- `is_active` BOOLEAN DEFAULT true
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

**Indexes:**
- `idx_countries_region_id` on region_id
- `idx_countries_iso2` on iso2
- `idx_countries_iso3` on iso3
- `idx_countries_is_active` on is_active
- `idx_countries_name` on name

#### 3. **states**
- `id` BIGSERIAL PRIMARY KEY
- `country_id` BIGINT NOT NULL (FK to countries)
- `name` VARCHAR(100) NOT NULL
- `state_code` VARCHAR(10) (e.g., CA, NY, PB)
- `is_active` BOOLEAN DEFAULT true
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

**Indexes:**
- `idx_states_country_id` on country_id
- `idx_states_state_code` on state_code
- `idx_states_is_active` on is_active
- `idx_states_name` on name

#### 4. **cities**
- `id` BIGSERIAL PRIMARY KEY
- `state_id` BIGINT NOT NULL (FK to states)
- `name` VARCHAR(100) NOT NULL
- `latitude` DECIMAL(10, 8)
- `longitude` DECIMAL(11, 8)
- `is_active` BOOLEAN DEFAULT true
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

**Indexes:**
- `idx_cities_state_id` on state_id
- `idx_cities_is_active` on is_active
- `idx_cities_name` on name
- `idx_cities_coordinates` on (latitude, longitude)

#### 5. **addresses**
- `id` UUID PRIMARY KEY
- `city_id` BIGINT NOT NULL (FK to cities)
- `address_line1` VARCHAR(255) NOT NULL
- `address_line2` VARCHAR(255)
- `street_no` VARCHAR(50)
- `area` VARCHAR(100)
- `division` VARCHAR(100)
- `latitude` DECIMAL(10, 8)
- `longitude` DECIMAL(11, 8)
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

**Indexes:**
- `idx_addresses_city_id` on city_id
- `idx_addresses_coordinates` on (latitude, longitude)

### Modifications to Existing Tables

#### **stores** table
**Removed columns:**
- `street_address`
- `city`
- `state`
- `country`
- `postal_code`

**Added columns:**
- `address_id` UUID (FK to addresses, nullable)

**New constraint:**
- `fk_stores_address` foreign key to addresses(id) ON DELETE SET NULL

**New index:**
- `idx_stores_address_id` on address_id

## Seeded Data

### Regions (7 total)
- Africa (AF)
- Asia (AS)
- Europe (EU)
- North America (NA)
- South America (SA)
- Oceania (OC)
- Antarctica (AN)

### Countries (36 total)
**Asia (15 countries):**
- Pakistan (PK/PAK, +92, Urdu/ur)
- India (IN/IND, +91, Hindi/hi)
- Bangladesh (BD/BGD, +880, Bengali/bn)
- China (CN/CHN, +86, Mandarin/zh)
- Japan (JP/JPN, +81, Japanese/ja)
- South Korea (KR/KOR, +82, Korean/ko)
- Indonesia (ID/IDN, +62, Indonesian/id)
- Thailand (TH/THA, +66, Thai/th)
- Malaysia (MY/MYS, +60, Malay/ms)
- Singapore (SG/SGP, +65, English/en)
- Philippines (PH/PHL, +63, Filipino/fil)
- Vietnam (VN/VNM, +84, Vietnamese/vi)
- Turkey (TR/TUR, +90, Turkish/tr)
- Saudi Arabia (SA/SAU, +966, Arabic/ar)
- United Arab Emirates (AE/ARE, +971, Arabic/ar)

**North America (3 countries):**
- United States (US/USA, +1, English/en)
- Canada (CA/CAN, +1, English/en)
- Mexico (MX/MEX, +52, Spanish/es)

**Europe (8 countries):**
- United Kingdom (GB/GBR, +44, English/en)
- Germany (DE/DEU, +49, German/de)
- France (FR/FRA, +33, French/fr)
- Italy (IT/ITA, +39, Italian/it)
- Spain (ES/ESP, +34, Spanish/es)
- Netherlands (NL/NLD, +31, Dutch/nl)
- Poland (PL/POL, +48, Polish/pl)
- Russia (RU/RUS, +7, Russian/ru)

**Africa (4 countries):**
- Nigeria (NG/NGA, +234, English/en)
- South Africa (ZA/ZAF, +27, English/en)
- Egypt (EG/EGY, +20, Arabic/ar)
- Kenya (KE/KEN, +254, Swahili/sw)

**South America (4 countries):**
- Brazil (BR/BRA, +55, Portuguese/pt)
- Argentina (AR/ARG, +54, Spanish/es)
- Colombia (CO/COL, +57, Spanish/es)
- Chile (CL/CHL, +56, Spanish/es)

**Oceania (2 countries):**
- Australia (AU/AUS, +61, English/en)
- New Zealand (NZ/NZL, +64, English/en)

### States
**Pakistan (7 states):**
- Punjab (PB)
- Sindh (SD)
- Khyber Pakhtunkhwa (KP)
- Balochistan (BA)
- Islamabad Capital Territory (IS)
- Gilgit-Baltistan (GB)
- Azad Jammu and Kashmir (JK)

**United States (10 states):**
- California (CA)
- Texas (TX)
- Florida (FL)
- New York (NY)
- Illinois (IL)
- Pennsylvania (PA)
- Ohio (OH)
- Georgia (GA)
- North Carolina (NC)
- Michigan (MI)

### Cities
**Punjab, Pakistan (7 cities):**
- Lahore (31.5204, 74.3587)
- Faisalabad (31.4180, 73.0790)
- Rawalpindi (33.5651, 73.0169)
- Multan (30.1575, 71.5249)
- Gujranwala (32.1617, 74.1883)
- Sialkot (32.4945, 74.5229)
- Bahawalpur (29.3956, 71.6836)

**Sindh, Pakistan (4 cities):**
- Karachi (24.8607, 67.0011)
- Hyderabad (25.3960, 68.3578)
- Sukkur (27.7058, 68.8574)
- Larkana (27.5590, 68.2120)

**Islamabad Capital Territory, Pakistan (1 city):**
- Islamabad (33.6844, 73.0479)

**California, USA (4 cities):**
- Los Angeles (34.0522, -118.2437)
- San Francisco (37.7749, -122.4194)
- San Diego (32.7157, -117.1611)
- San Jose (37.3382, -121.8863)

**New York, USA (3 cities):**
- New York City (40.7128, -74.0060)
- Buffalo (42.8864, -78.8784)
- Rochester (43.1566, -77.6088)

## API Endpoints

All endpoints are public (no authentication required) and support pagination.

### Base URL: `/api/public/address`

### Region Endpoints

#### 1. Get All Regions
```
GET /api/public/address/regions?page=0&size=20&search=asia
```
**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `search` (optional) - searches name and code

**Response:** Page<Region>

#### 2. Get Region by ID
```
GET /api/public/address/regions/{id}
```
**Response:** Region

#### 3. Get Region by Code
```
GET /api/public/address/regions/code/{code}
```
**Example:** `GET /api/public/address/regions/code/AS`
**Response:** Region

### Country Endpoints

#### 1. Get All Countries
```
GET /api/public/address/countries?page=0&size=20&regionId=1&search=pak
```
**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `regionId` (optional) - filter by region
- `search` (optional) - searches name, iso2, and iso3

**Response:** Page<Country>

#### 2. Get Country by ID
```
GET /api/public/address/countries/{id}
```
**Response:** Country

#### 3. Get Country by ISO2 Code
```
GET /api/public/address/countries/iso2/{iso2}
```
**Example:** `GET /api/public/address/countries/iso2/PK`
**Response:** Country

#### 4. Get Country by ISO3 Code
```
GET /api/public/address/countries/iso3/{iso3}
```
**Example:** `GET /api/public/address/countries/iso3/PAK`
**Response:** Country

### State Endpoints

#### 1. Get All States
```
GET /api/public/address/states?page=0&size=20&countryId=1&search=punjab
```
**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `countryId` (optional) - filter by country
- `search` (optional) - searches name and state_code

**Response:** Page<State>

#### 2. Get State by ID
```
GET /api/public/address/states/{id}
```
**Response:** State

### City Endpoints

#### 1. Get All Cities
```
GET /api/public/address/cities?page=0&size=20&stateId=1&search=lahore
```
**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `stateId` (optional) - filter by state
- `search` (optional) - searches name

**Response:** Page<City>

#### 2. Get City by ID
```
GET /api/public/address/cities/{id}
```
**Response:** City

## Entity Structure

### Region Entity
```kotlin
data class Region(
    val id: Long?,
    val name: String,
    val code: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Country Entity
```kotlin
data class Country(
    val id: Long?,
    val regionId: Long,
    val name: String,
    val iso2: String,
    val iso3: String,
    val phoneCode: String?,
    val language: String?,
    val languageCode: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### State Entity
```kotlin
data class State(
    val id: Long?,
    val countryId: Long,
    val name: String,
    val stateCode: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### City Entity
```kotlin
data class City(
    val id: Long?,
    val stateId: Long,
    val name: String,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Address Entity
```kotlin
data class Address(
    val id: UUID?,
    val cityId: Long,
    val addressLine1: String,
    val addressLine2: String?,
    val streetNo: String?,
    val area: String?,
    val division: String?,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

## Usage in Stores

### Updated Store Entity
The `Store` entity now references an `Address` instead of storing address fields directly:

```kotlin
data class Store(
    val id: UUID?,
    val storeOwnerProfileId: UUID,
    val name: String,
    val slug: String,
    val description: String?,
    val storeCategoryId: Long?,
    val locationType: LocationType,
    val addressId: UUID?,  // NEW: Reference to addresses table
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val phone: String?,
    val email: String?,
    val website: String?,
    val businessHours: String?,
    val isActive: Boolean,
    val isVerified: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Creating a Store with Address

**Workflow:**
1. User selects city from public API: `GET /api/public/address/cities?stateId=1`
2. Frontend creates an address (future API - not yet implemented)
3. User provides address details and gets addressId
4. User creates store with addressId:

```json
POST /api/storeowner/stores
{
  "name": "My Electronics Store",
  "description": "Best electronics in town",
  "storeCategoryId": 1,
  "locationType": "PHYSICAL",
  "addressId": "uuid-of-address",
  "latitude": 31.5204,
  "longitude": 74.3587,
  "phone": "+92300123456",
  "email": "store@example.com"
}
```

## Repository Methods

### RegionRepository
- `findByCode(code: String): Region?`
- `findAllActive(pageable: Pageable): Page<Region>`
- `searchByNameOrCode(search: String, pageable: Pageable): Page<Region>`

### CountryRepository
- `findByIso2(iso2: String): Country?`
- `findByIso3(iso3: String): Country?`
- `findByRegionId(regionId: Long, pageable: Pageable): Page<Country>`
- `findAllActive(pageable: Pageable): Page<Country>`
- `searchByNameOrCode(search: String, pageable: Pageable): Page<Country>`

### StateRepository
- `findByCountryId(countryId: Long, pageable: Pageable): Page<State>`
- `findAllActive(pageable: Pageable): Page<State>`
- `searchByNameOrCode(search: String, pageable: Pageable): Page<State>`

### CityRepository
- `findByStateId(stateId: Long, pageable: Pageable): Page<City>`
- `findAllActive(pageable: Pageable): Page<City>`
- `searchByName(search: String, pageable: Pageable): Page<City>`

### AddressRepository
- `findByCityId(cityId: Long, pageable: Pageable): Page<Address>`

## Service Layer

### AddressService Methods

**Regions:**
- `getAllRegions(pageable: Pageable): Page<Region>`
- `getRegionById(id: Long): Region?`
- `getRegionByCode(code: String): Region?`
- `searchRegions(search: String, pageable: Pageable): Page<Region>`

**Countries:**
- `getAllCountries(pageable: Pageable): Page<Country>`
- `getCountriesByRegion(regionId: Long, pageable: Pageable): Page<Country>`
- `getCountryById(id: Long): Country?`
- `getCountryByIso2(iso2: String): Country?`
- `getCountryByIso3(iso3: String): Country?`
- `searchCountries(search: String, pageable: Pageable): Page<Country>`

**States:**
- `getAllStates(pageable: Pageable): Page<State>`
- `getStatesByCountry(countryId: Long, pageable: Pageable): Page<State>`
- `getStateById(id: Long): State?`
- `searchStates(search: String, pageable: Pageable): Page<State>`

**Cities:**
- `getAllCities(pageable: Pageable): Page<City>`
- `getCitiesByState(stateId: Long, pageable: Pageable): Page<City>`
- `getCityById(id: Long): City?`
- `searchCities(search: String, pageable: Pageable): Page<City>`

**Addresses:**
- `getAddressesByCity(cityId: Long, pageable: Pageable): Page<Address>`

## Migration Files

All migrations are located in: `src/main/resources/db/changelog/v1.1.0/address/`

**Files:**
1. `create-regions-table.sql` - Creates regions table with indexes
2. `create-countries-table.sql` - Creates countries table with ISO codes
3. `create-states-table.sql` - Creates states table
4. `create-cities-table.sql` - Creates cities table with coordinates
5. `create-addresses-table.sql` - Creates addresses table
6. `seed-address-data.sql` - Seeds all address data (regions, countries, states, cities)
7. `modify-stores-table.sql` - Removes address fields from stores, adds address_id FK
8. `changelog-address.yaml` - Liquibase changelog that applies all migrations

**Included in:** `src/main/resources/db/changelog/changes/changelog-v1.1.0.yaml`

## Future Enhancements

1. **Address Management API** (authenticated)
   - POST /api/storeowner/addresses - Create address
   - PUT /api/storeowner/addresses/{id} - Update address
   - DELETE /api/storeowner/addresses/{id} - Delete address

2. **Address Validation**
   - Validate city_id exists before creating address
   - Validate latitude/longitude ranges
   - Validate required fields based on country

3. **Geocoding Integration**
   - Automatically set latitude/longitude from address
   - Address autocomplete API

4. **Extended Data**
   - Add more countries, states, and cities
   - Add postal code validation patterns per country
   - Add time zones to countries/states

5. **Address Search**
   - Full-text search across all address fields
   - Nearby location search using coordinates

## Testing

The address system was successfully compiled and built. Integration tests should be added to verify:

1. Region, country, state, city retrieval with pagination
2. Search functionality for all entities
3. Filtering countries by region, states by country, cities by state
4. ISO code lookups (iso2, iso3)
5. Store creation with addressId reference

## Related Documentation

- [Store Owner Implementation](STORE_OWNER_IMPLEMENTATION_COMPLETE.md)
- [CLAUDE.md](CLAUDE.md) - Project architecture and patterns
- [HELP.md](HELP.md) - API documentation

---

**Implementation Date:** 2026-02-15
**Version:** 1.1.0
**Status:** ✅ Complete and Built Successfully
