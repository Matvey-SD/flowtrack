package ru.sarapulov.demos.security;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.sarapulov.demos.services.UserService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private UserService userService;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)
            .passwordEncoder(passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests((authorize) -> authorize.requestMatchers("/", "/signUp", "/css/**", "/js/**", "/images/**", "/api")
                                                           .permitAll()
                                                           .anyRequest()
                                                           .authenticated())
            .formLogin((login) -> login.loginPage("/signIn")
                                       .usernameParameter("login")
                                       .passwordParameter("password")
                                       .defaultSuccessUrl("/home")
                                       .failureUrl("/signIn")
                                       .permitAll())
            .logout((logout) -> logout.logoutSuccessUrl("/")
                                      .permitAll());

        return http.build();
    }

}
