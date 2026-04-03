package com.alok.Expense_Tracker.Security;

import com.alok.Expense_Tracker.entity.type.AuthProviderType;
import com.alok.Expense_Tracker.Repository.UserRepository;
import com.alok.Expense_Tracker.dto.LoginRequestDTO;
import com.alok.Expense_Tracker.dto.LoginResponseDTO;
import com.alok.Expense_Tracker.dto.SignupRequestDTO;
import com.alok.Expense_Tracker.dto.SignupResponseDTO;
import com.alok.Expense_Tracker.entity.User;

import com.alok.Expense_Tracker.entity.type.RoleType;
import com.alok.Expense_Tracker.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUtil authUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public LoginResponseDTO login(LoginRequestDTO request) {
     Authentication authentication;
        try {
             authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        User user = (User)authentication.getPrincipal();
        String token=authUtil.generateAccessToken(user);
        return new LoginResponseDTO(token,user.getId());
    }

    public User SignUpInternal( SignupRequestDTO signupRequestDTO,String providerId, AuthProviderType authProviderType){
        User user=userRepository.findByUsername(signupRequestDTO.getUsername()).orElse(null);
        if(user!=null){
            throw new IllegalArgumentException("User already exists");
        }

        if (userRepository.existsByUsername(signupRequestDTO.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

         user = User.builder()
                .name(signupRequestDTO.getName())
                .username(signupRequestDTO.getUsername())
                .email(signupRequestDTO.getUsername())
                .providerId(providerId)
                .providerType(authProviderType)
                .roles(Set.of(RoleType.USER))
                .build();


        if(authProviderType == AuthProviderType.EMAIL){
              user.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));
        } else {
            user.setPassword(UUID.randomUUID().toString());
        }
       userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        return user;
    }
    public SignupResponseDTO SignUp(SignupRequestDTO signupRequestDTO) {
       User user=SignUpInternal(signupRequestDTO,null,AuthProviderType.EMAIL);

      return new SignupResponseDTO(user.getId(),user.getUsername());
    }

    public ResponseEntity<LoginResponseDTO> HandleOAuth2LoginRequest(String registrationId, OAuth2User oAuth2User){

        AuthProviderType ProviderType=authUtil.FindProviderType(registrationId);
        String ProviderId=authUtil.FindProviderId(oAuth2User,registrationId);

        User user=userRepository.findByProviderTypeAndProviderId(ProviderType,ProviderId).orElse(null);
        String email=oAuth2User.getAttribute("email");
        String name=oAuth2User.getAttribute("name");

        User EmailUser=userRepository.findByUsername(email).orElse(null);

        if(user==null && EmailUser==null){

            String userName=authUtil.determineUsernameFromOAuth2User(oAuth2User,registrationId,ProviderId);
            user=SignUpInternal(new SignupRequestDTO(userName,null,name), ProviderId,ProviderType);
        }
        else if(user!=null){
          if(email!=null && !email.isBlank() && !email.equals(user.getUsername())){
              user.setName(name);
              userRepository.save(user);
          }
        }
        else{
            throw new BadCredentialsException("This User is alredy registerd with type :" + EmailUser .getProviderType());
        }

        LoginResponseDTO loginResponseDTO=new  LoginResponseDTO(authUtil.generateAccessToken(user),user.getId() );
        return ResponseEntity.ok(loginResponseDTO);
    }

}
