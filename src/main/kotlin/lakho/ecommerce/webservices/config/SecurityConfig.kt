package lakho.ecommerce.webservices.config

import lakho.ecommerce.webservices.auth.services.JwtService
import lakho.ecommerce.webservices.auth.services.JwtTokenCacheService
import lakho.ecommerce.webservices.auth.services.OAuth2AuthenticationSuccessHandler
import lakho.ecommerce.webservices.config.security.CustomAccessDeniedHandler
import lakho.ecommerce.webservices.config.security.CustomAuthenticationEntryPoint
import lakho.ecommerce.webservices.config.security.filters.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
internal class SecurityConfig(
    private val jwtService: JwtService,
    private val jwtTokenCacheService: JwtTokenCacheService,
    private val oauth2SuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val accessDeniedHandler: CustomAccessDeniedHandler
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers ->
                headers
                    .xssProtection { xss ->
                        xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                    }
                    .contentSecurityPolicy { csp ->
                        csp.policyDirectives("default-src 'self'")
                    }
                    .frameOptions { frame ->
                        frame.deny()
                    }
                    .httpStrictTransportSecurity { hsts ->
                        hsts.maxAgeInSeconds(31536000)
                            .includeSubDomains(true)
                            .preload(true)
                    }
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/storeowner/**").hasRole("STORE_OWNER")
                    .requestMatchers("/api/consumer/**").hasRole("CONSUMER")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .successHandler(oauth2SuccessHandler)
                    .failureUrl("/login?error=true")
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtService, jwtTokenCacheService),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()


    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}