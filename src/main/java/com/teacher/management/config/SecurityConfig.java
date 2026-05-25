package com.teacher.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final String API_KEY_HEADER = "X-API-KEY";
        private static final String VALID_API_KEY = "hubspot-secret-api-key-2026"; // 하드코딩된 인증키

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                OncePerRequestFilter apiKeyFilter = new OncePerRequestFilter() {
                        @Override
                        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                                        throws ServletException, IOException {
                                
                                String path = request.getRequestURI();
                                // /api/html/ 이하의 경로에만 API 키 검사 적용
                                if (path.startsWith("/api/html/")) {
                                        String reqApiKey = request.getHeader(API_KEY_HEADER);
                                        if (!VALID_API_KEY.equals(reqApiKey)) {
                                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                response.setContentType("application/json;charset=UTF-8");
                                                response.getWriter().write("{\"error\": \"Invalid or missing API Key\"}");
                                                return; // 인증 실패 시 진행 중단
                                        }
                                }
                                filterChain.doFilter(request, response);
                        }
                };

                http
                                .addFilterBefore(apiKeyFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/html/**"))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/api/html/**", "/html/**", "/error")
                                                .permitAll() // Static resources
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
