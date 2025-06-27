package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails jfdg = User.withUsername("jfdg")
                .roles("USUARIO").password("{noop}admin").build();
        UserDetails admin = User.withUsername("admin")
                .roles("ADMIN", "USUARIO").password("{noop}admin").build();
        return new InMemoryUserDetailsManager(jfdg, admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {
                })
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        // 2. Set your SvelteKit app's origin
//        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
//        // 3. Allow all standard methods
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//        // 4. Allow necessary headers, including Authorization
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
//        // 5. Allow credentials
//        configuration.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // 6. Apply this configuration to all routes
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}
