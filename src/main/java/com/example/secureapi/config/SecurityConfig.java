package com.example.secureapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * "Login" is name-only: any non-blank string is accepted as a reader identity.
 * The password field on the form is a dummy — this encoder always matches.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/metrics"
                        ).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(
                                "/css/**",
                                "/login",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/v1/status").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(f -> f
                        .loginPage("/login")
                        .defaultSuccessUrl("/books", true)
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                )
                // H2 console requires same-origin framing; disable only that restriction,
                // not the full clickjacking protection.
                .headers(h -> h.frameOptions(fo -> fo.sameOrigin()))
                .csrf(c -> c.ignoringRequestMatchers("/h2-console/**"))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence raw) {
                return "DUMMY";
            }

            @Override
            public boolean matches(CharSequence raw, String encoded) {
                return true;
            }
        };
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder pe) {
        return username -> {
            if (username == null || username.isBlank()) {
                throw new UsernameNotFoundException("name required");
            }
            String name = username.trim();
            if (name.length() > 64) {
                name = name.substring(0, 64);
            }
            UserDetails user = User
                    .withUsername(name)
                    .password(pe.encode("x"))
                    .roles("READER")
                    .build();
            return user;
        };
    }

}
