package com.alok.Expense_Tracker.Security;

import com.alok.Expense_Tracker.entity.type.AuthProviderType;
import com.alok.Expense_Tracker.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component

public class AuthUtil {
    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey(){
      return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }
    public String generateAccessToken(User user) {
         return Jwts.builder()
                 .subject(user.getUsername())
                 .claim("userid", user.getId().toString())
                 .issuedAt(new Date())
                 .expiration(new Date(System.currentTimeMillis()+1000*60*60*24))
                 .signWith(getSecretKey())
                 .compact();
    }

    public String findUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();

        } catch (Exception e) {
            log.error("Invalid JWT: {}", e.getMessage());
            return null;
        }
    }
    public AuthProviderType FindProviderType(String registrationId){
        return switch (registrationId.toLowerCase()){
            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            default -> throw new IllegalArgumentException("Invalid OAuth2ProviderType : {}"+registrationId);
        };
    }

    public String FindProviderId(OAuth2User oAuth2User,String registrationId){
        return switch (registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("id").toString();
            default -> {
                log.error("Unsupported OAuth2ProviderType : {} "+registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
            }
        };
    }

    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User,String registrationId,String providerId){
        String email=oAuth2User.getAttribute("email");
        if(email!=null && !email.isBlank() ){
            return email;
        }
        return switch (registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }
}
