package com.flores.oauth2login.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                        .requestMatchers("/user-info", "/contacts").authenticated()
                        .anyRequest().permitAll()
                    )
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/user-info", true))
                .logout(logout -> logout.logoutSuccessUrl("/login"))
                .formLogin(form -> form.defaultSuccessUrl("/secured", true))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
