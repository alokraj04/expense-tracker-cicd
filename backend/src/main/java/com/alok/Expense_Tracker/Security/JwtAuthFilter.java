package com.alok.Expense_Tracker.Security;

import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.entity.User;
import com.alok.Expense_Tracker.entity.UserPrinciple;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
         log.info("Incoming request: {}",request.getRequestURI());

        String path = request.getRequestURI();
        log.info("Incoming request: {}", path);
        if (path.startsWith("/oauth2") ||
                path.startsWith("/login") ||
                path.startsWith("/auth") ||
                path.startsWith("/error")) {

            filterChain.doFilter(request, response);
            return;
        }
         final String requestTokenGenerator=request.getHeader("Authorization");
         if(requestTokenGenerator ==null || !requestTokenGenerator.startsWith("Bearer ")){
             filterChain.doFilter(request,response);
             return;
         }
        String token = requestTokenGenerator.substring(7);
         String Username=authUtil.findUsernameFromToken(token);
          log.info("Extracted username from token: {}",Username);
         if(Username !=null && SecurityContextHolder.getContext().getAuthentication()==null){
             User user= userRepository.findByUsername(Username).orElse(null);
           UserPrinciple userPrinciple = new UserPrinciple(user);
             UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
                     new UsernamePasswordAuthenticationToken(userPrinciple,null,userPrinciple.getAuthorities());
             SecurityContextHolder.getContext()
                     .setAuthentication(usernamePasswordAuthenticationToken);

         }
         filterChain.doFilter(request,response);
    }
}



