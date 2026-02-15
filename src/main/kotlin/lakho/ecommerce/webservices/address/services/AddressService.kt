package lakho.ecommerce.webservices.address.services

import lakho.ecommerce.webservices.address.repositories.*
import lakho.ecommerce.webservices.address.repositories.entities.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AddressService(
    private val regionRepository: RegionRepository,
    private val countryRepository: CountryRepository,
    private val stateRepository: StateRepository,
    private val cityRepository: CityRepository,
    private val addressRepository: AddressRepository
) {

    // Region operations
    fun getAllRegions(pageable: Pageable): Page<Region> {
        return regionRepository.findAllActive(pageable)
    }

    fun getRegionById(id: Long): Region? {
        return regionRepository.findById(id).orElse(null)
    }

    fun getRegionByCode(code: String): Region? {
        return regionRepository.findByCode(code)
    }

    fun searchRegions(search: String, pageable: Pageable): Page<Region> {
        return regionRepository.searchByNameOrCode(search, pageable)
    }

    // Country operations
    fun getAllCountries(pageable: Pageable): Page<Country> {
        return countryRepository.findAllActive(pageable)
    }

    fun getCountriesByRegion(regionId: Long, pageable: Pageable): Page<Country> {
        return countryRepository.findByRegionId(regionId, pageable)
    }

    fun getCountryById(id: Long): Country? {
        return countryRepository.findById(id).orElse(null)
    }

    fun getCountryByIso2(iso2: String): Country? {
        return countryRepository.findByIso2(iso2)
    }

    fun getCountryByIso3(iso3: String): Country? {
        return countryRepository.findByIso3(iso3)
    }

    fun searchCountries(search: String, pageable: Pageable): Page<Country> {
        return countryRepository.searchByNameOrCode(search, pageable)
    }

    // State operations
    fun getAllStates(pageable: Pageable): Page<State> {
        return stateRepository.findAllActive(pageable)
    }

    fun getStatesByCountry(countryId: Long, pageable: Pageable): Page<State> {
        return stateRepository.findByCountryId(countryId, pageable)
    }

    fun getStateById(id: Long): State? {
        return stateRepository.findById(id).orElse(null)
    }

    fun searchStates(search: String, pageable: Pageable): Page<State> {
        return stateRepository.searchByNameOrCode(search, pageable)
    }

    // City operations
    fun getAllCities(pageable: Pageable): Page<City> {
        return cityRepository.findAllActive(pageable)
    }

    fun getCitiesByState(stateId: Long, pageable: Pageable): Page<City> {
        return cityRepository.findByStateId(stateId, pageable)
    }

    fun getCityById(id: Long): City? {
        return cityRepository.findById(id).orElse(null)
    }

    fun searchCities(search: String, pageable: Pageable): Page<City> {
        return cityRepository.searchByName(search, pageable)
    }

    // Address operations
    fun getAddressesByCity(cityId: Long, pageable: Pageable): Page<Address> {
        return addressRepository.findByCityId(cityId, pageable)
    }
}
