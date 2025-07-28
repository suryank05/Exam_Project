package com.ExamPort.ExamPort.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(java.util.List.of("http://localhost:5173", "http://localhost:3000"));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                corsConfig.setMaxAge(3600L);
                return corsConfig;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**", "/welcome", "/health", "/api/courses/public", "/api/test/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                
                // Course endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/courses/public").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/courses/create").hasAnyRole("ADMIN", "INSTRUCTOR")
                .requestMatchers("/api/courses/**").authenticated()
                
                // Exam endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/exam", "/exam/**", "/api/exams", "/api/exams/**").hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/exam", "/exam/**", "/api/exams", "/api/exams/**").hasAnyRole("ADMIN", "STUDENT", "INSTRUCTOR")
                
                // User endpoints
                .requestMatchers("/api/users/me").authenticated()
                
                // Enrollment endpoints
                .requestMatchers("/api/enrollments/**").authenticated()
                
                // Payment endpoints
                .requestMatchers("/api/payments/**").authenticated()
                
                // Allow contact form submissions without authentication
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/contact").permitAll()

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
