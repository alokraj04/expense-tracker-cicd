package com.alok.Expense_Tracker.Security;

import com.alok.Expense_Tracker.dto.LoginResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@AllArgsConstructor

public class OAuth2successHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = token.getPrincipal();
            String registrationId = token.getAuthorizedClientRegistrationId();

            ResponseEntity<LoginResponseDTO> loginResponse =
                    authService.HandleOAuth2LoginRequest(registrationId, oAuth2User);

            LoginResponseDTO body = loginResponse.getBody();

            String redirectUrl = "http://localhost:5173/oauth2/redirect"
                    + "?token=" + body.getAccessToken()
                    + "&userId=" + body.getUserId();

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:5173/login?error=oauth_failed");
        }
    }
}
