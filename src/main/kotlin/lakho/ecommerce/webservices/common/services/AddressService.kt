package lakho.ecommerce.webservices.common.services

import lakho.ecommerce.webservices.common.CustomPage
import lakho.ecommerce.webservices.common.repositories.AddressRepository
import lakho.ecommerce.webservices.common.repositories.CityRepository
import lakho.ecommerce.webservices.common.repositories.CountryRepository
import lakho.ecommerce.webservices.common.repositories.RegionRepository
import lakho.ecommerce.webservices.common.repositories.StateRepository
import lakho.ecommerce.webservices.common.repositories.entities.Address
import lakho.ecommerce.webservices.common.repositories.entities.City
import lakho.ecommerce.webservices.common.repositories.entities.Country
import lakho.ecommerce.webservices.common.repositories.entities.Region
import lakho.ecommerce.webservices.common.repositories.entities.State
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
    fun getAllRegions(pageable: Pageable): CustomPage<Region> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = regionRepository.findAllActive(pageable.pageSize, offset)
        val totalElements = regionRepository.countAllActive()
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    fun getRegionById(id: Long): Region? {
        return regionRepository.findById(id).orElse(null)
    }

    fun getRegionByCode(code: String): Region? {
        return regionRepository.findByCode(code)
    }

    fun searchRegions(search: String, pageable: Pageable): CustomPage<Region> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = regionRepository.searchByNameOrCode(search, pageable.pageSize, offset)
        val totalElements = regionRepository.countSearchByNameOrCode(search)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    // Country operations
    fun getAllCountries(pageable: Pageable): CustomPage<Country> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = countryRepository.findAllActive(pageable.pageSize, offset)
        val totalElements = countryRepository.countAllActive()
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    fun getCountriesByRegion(regionId: Long, pageable: Pageable): CustomPage<Country> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = countryRepository.findByRegionId(regionId, pageable.pageSize, offset)
        val totalElements = countryRepository.countByRegionId(regionId)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
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

    fun searchCountries(search: String, pageable: Pageable): CustomPage<Country> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = countryRepository.searchByNameOrCode(search, pageable.pageSize, offset)
        val totalElements = countryRepository.countSearchByNameOrCode(search)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    // State operations
    fun getAllStates(pageable: Pageable): CustomPage<State> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = stateRepository.findAllActive(pageable.pageSize, offset)
        val totalElements = stateRepository.countAllActive()
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    fun getStatesByCountry(countryId: Long, pageable: Pageable): CustomPage<State> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = stateRepository.findByCountryId(countryId, pageable.pageSize, offset)
        val totalElements = stateRepository.countByCountryId(countryId)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    fun getStateById(id: Long): State? {
        return stateRepository.findById(id).orElse(null)
    }

    fun searchStates(search: String, pageable: Pageable): CustomPage<State> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = stateRepository.searchByNameOrCode(search, pageable.pageSize, offset)
        val totalElements = stateRepository.countSearchByNameOrCode(search)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    // City operations
    fun getAllCities(pageable: Pageable): CustomPage<City> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = cityRepository.findAllActive(pageable.pageSize, offset)
        val totalElements = cityRepository.countAllActive()
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    fun getCitiesByState(stateId: Long, pageable: Pageable): CustomPage<City> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = cityRepository.findByStateId(stateId, pageable.pageSize, offset)
        val totalElements = cityRepository.countByStateId(stateId)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    fun getCityById(id: Long): City? {
        return cityRepository.findById(id).orElse(null)
    }

    fun searchCities(search: String, pageable: Pageable): CustomPage<City> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = cityRepository.searchByName(search, pageable.pageSize, offset)
        val totalElements = cityRepository.countSearchByName(search)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }

    // Address operations
    fun getAddressesByCity(cityId: Long, pageable: Pageable): CustomPage<Address> {
        val offset = pageable.pageNumber * pageable.pageSize.toLong()
        val content = addressRepository.findByCityId(cityId, pageable.pageSize, offset)
        val totalElements = addressRepository.countByCityId(cityId)
        return CustomPage.Companion.of(content, totalElements, pageable.pageNumber, pageable.pageSize)
    }
}