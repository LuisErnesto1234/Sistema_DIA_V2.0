package com.spring.udemy.controlagua.config;

import com.spring.udemy.controlagua.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SeguridadConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.
                authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/control/**").hasRole("ADMIN")
                        .requestMatchers("/usuario/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/login", "/registro","/css/**","/", "/js/**", "/icons/**", "/images/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/inicio", true)
                        .permitAll())
//                .rememberMe(remember -> remember
//                        .key("Virtual2023") // Clave secreta para cifrar el token
//                        .tokenValiditySeconds(30 * 24 * 60 * 60)) // 30 días en segundos
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(exception ->
                        exception.accessDeniedPage("/acceso-denegado")
                )
                .userDetailsService(usuarioDetailsService)
                .build();
    }
}
