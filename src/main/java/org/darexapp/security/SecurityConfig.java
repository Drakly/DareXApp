package org.darexapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Позволяваме публичен достъп до избрани URL-та:
                        .requestMatchers("/", "/index", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        // Всички останали заявки изискват автентикация
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")  // дефинираме персонализирана login страница
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}
