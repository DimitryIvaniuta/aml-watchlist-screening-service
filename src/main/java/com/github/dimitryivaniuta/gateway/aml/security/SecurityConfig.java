package com.github.dimitryivaniuta.gateway.aml.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security:
 * - Basic Auth for demo/testing
 * - RBAC via roles: OPS_REQUESTER, OPS_APPROVER, AUDITOR
 * - Swagger/OpenAPI endpoints are public
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityUsersProperties.class)
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        // Public in this demo
        .requestMatchers(HttpMethod.POST, "/api/transactions").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/watchlists/import").permitAll()
        // Restricted
        .requestMatchers("/api/audit/**").hasRole("AUDITOR")
        .requestMatchers("/api/dlq/**").authenticated()
        .anyRequest().permitAll()
    );

    http.httpBasic(withDefaults());
    return http.build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsService(SecurityUsersProperties props) {
    List<UserDetails> users = props.users().stream()
        .map(u -> User.withUsername(u.username())
            // {noop} for demo only
            .password("{noop}" + u.password())
            .roles(u.roles().toArray(String[]::new))
            .build())
        .toList();
    return new InMemoryUserDetailsManager(users);
  }
}
