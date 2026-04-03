package com.alok.Expense_Tracker.Config;

import com.alok.Expense_Tracker.Security.JwtAuthFilter;
import com.alok.Expense_Tracker.Security.OAuth2successHandler;
import com.alok.Expense_Tracker.entity.type.RoleType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static com.alok.Expense_Tracker.entity.type.RoleType.*;


@Configuration
@RequiredArgsConstructor
public class WebConfig {


    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2successHandler oAuth2successHandler;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/auditor/**").hasAnyRole(AUDITOR.name(), ADMIN.name())
                        .requestMatchers("/users/**").hasAnyRole(USER.name(), ADMIN.name())
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers(
                                        "/api/auth/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs.yaml"
                                ).permitAll()
                        .anyRequest().authenticated())

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2successHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 login failed: {}", exception.getMessage());
                            response.sendRedirect("http://localhost:5173/login?error=oauth2");
                        }))

                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        handlerExceptionResolver.resolveException(
                                                request, response, null, accessDeniedException))
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        handlerExceptionResolver.resolveException(
                                                request, response, null, authException)));

        return http.build();
    }
}
