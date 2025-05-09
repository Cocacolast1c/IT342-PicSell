package com.PicSell_IT342.PicSell.Security;

import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // ADDED
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;

    @Autowired
    public SecurityConfig(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            FormLoginSuccessHandler formLoginSuccessHandler,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            AuthenticationFailureHandler loginFailureHandler
    ) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/", "/login", "/oauth2/**", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/register", "/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET,"/images", "/images/search", "/images/{id}").permitAll()
                        .requestMatchers("/token/test").permitAll()

                        // Admin endpoints (restrict to ROLE_ADMIN)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/images/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/images/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/images").authenticated()

                        // Other authenticated endpoints
                        .requestMatchers("/inventory/**", "/transactions/**", "/notifications/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/{id}").authenticated()

                        // Default: Any other request needs authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login").loginProcessingUrl("/login")
                        .successHandler(formLoginSuccessHandler).failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login").successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt -> jwt
                                .decoder(this.jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()) // Use the custom converter bean
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.writeValue(response.getWriter(), Map.of("message", "Logout successful"));
                            response.getWriter().flush();
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("[❌ LOGIN FAILED] Reason: " + exception.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Authentication Failed", "message", exception.getMessage()));
            response.getWriter().flush();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            UserModel user = userRepository.findByUsername(username);
            if (user == null) {
                user = userRepository.findByEmail(username);
                if (user == null) {
                    throw new UsernameNotFoundException("User not found with username or email: " + username);
                }
            }
            String role = user.getRole() != null ? (user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole()) : "ROLE_USER";
            String principalName = user.getUsername() != null ? user.getUsername() : user.getEmail();
            String password = user.getPassword() != null ? user.getPassword() : "";

            return new org.springframework.security.core.userdetails.User(
                    principalName,
                    password,
                    List.of(new SimpleGrantedAuthority(role))
            );
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper;
    }
}