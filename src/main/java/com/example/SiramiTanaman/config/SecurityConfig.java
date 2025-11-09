package com.example.SiramiTanaman.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.SiramiTanaman.security.DbUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final DbUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authenticationProvider(authProvider())
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/**", "/sensor/**", "/device/**")  // disable CSRF for ESP endpoints
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/login",
                "/register",
                "/css/**",
                "/js/**",
                "/img/**",
                "/images/**",
                "/webjars/**"
            ).permitAll()
            .requestMatchers("/dashboard/**").authenticated()
            .anyRequest().permitAll()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        );

    return http.build();
}



    // Optional if you need the AuthenticationManager injected somewhere else
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
