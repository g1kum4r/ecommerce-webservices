package lakho.ecommerce.webservices.public.api

import lakho.ecommerce.webservices.address.repositories.entities.*
import lakho.ecommerce.webservices.address.services.AddressService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/public/address")
class AddressController(private val addressService: AddressService) {

    // Region endpoints
    @GetMapping("/regions")
    fun getRegions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<Page<Region>> {
        val pageable = PageRequest.of(page, size)
        val result = if (search.isNullOrBlank()) {
            addressService.getAllRegions(pageable)
        } else {
            addressService.searchRegions(search, pageable)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/regions/{id}")
    fun getRegionById(@PathVariable id: Long): ResponseEntity<Region> {
        val region = addressService.getRegionById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(region)
    }

    @GetMapping("/regions/code/{code}")
    fun getRegionByCode(@PathVariable code: String): ResponseEntity<Region> {
        val region = addressService.getRegionByCode(code)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(region)
    }

    // Country endpoints
    @GetMapping("/countries")
    fun getCountries(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) regionId: Long?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<Page<Country>> {
        val pageable = PageRequest.of(page, size)
        val result = when {
            !search.isNullOrBlank() -> addressService.searchCountries(search, pageable)
            regionId != null -> addressService.getCountriesByRegion(regionId, pageable)
            else -> addressService.getAllCountries(pageable)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/countries/{id}")
    fun getCountryById(@PathVariable id: Long): ResponseEntity<Country> {
        val country = addressService.getCountryById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(country)
    }

    @GetMapping("/countries/iso2/{iso2}")
    fun getCountryByIso2(@PathVariable iso2: String): ResponseEntity<Country> {
        val country = addressService.getCountryByIso2(iso2)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(country)
    }

    @GetMapping("/countries/iso3/{iso3}")
    fun getCountryByIso3(@PathVariable iso3: String): ResponseEntity<Country> {
        val country = addressService.getCountryByIso3(iso3)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(country)
    }

    // State endpoints
    @GetMapping("/states")
    fun getStates(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) countryId: Long?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<Page<State>> {
        val pageable = PageRequest.of(page, size)
        val result = when {
            !search.isNullOrBlank() -> addressService.searchStates(search, pageable)
            countryId != null -> addressService.getStatesByCountry(countryId, pageable)
            else -> addressService.getAllStates(pageable)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/states/{id}")
    fun getStateById(@PathVariable id: Long): ResponseEntity<State> {
        val state = addressService.getStateById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(state)
    }

    // City endpoints
    @GetMapping("/cities")
    fun getCities(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) stateId: Long?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<Page<City>> {
        val pageable = PageRequest.of(page, size)
        val result = when {
            !search.isNullOrBlank() -> addressService.searchCities(search, pageable)
            stateId != null -> addressService.getCitiesByState(stateId, pageable)
            else -> addressService.getAllCities(pageable)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/cities/{id}")
    fun getCityById(@PathVariable id: Long): ResponseEntity<City> {
        val city = addressService.getCityById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(city)
    }
}
