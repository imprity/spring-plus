package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // .csrf().disable() 방식은 더 이상 사용 안함.
                .csrf(AbstractHttpConfigurer::disable)
                // BasicAuthenticationFilter 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // UsernamePasswordAuthenticationFilter, DefaultLoginPageGeneratingFilter 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, SecurityContextHolderAwareRequestFilter.class)
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        .requestMatchers("/auth/signup").permitAll()
                                        .requestMatchers("/auth/signin").permitAll()
                                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN")
                                        .anyRequest().authenticated()
                );

        http.sessionManagement(
                (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
