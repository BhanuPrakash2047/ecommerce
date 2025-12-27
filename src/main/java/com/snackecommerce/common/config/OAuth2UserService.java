package com.snackecommerce.common.config;

import com.snackecommerce.user.entity.User;
import com.snackecommerce.user.enums.AuthProvider;
import com.snackecommerce.user.enums.UserRole;
import com.snackecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Check if user already exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Create new user
            user = User.builder()
                    .email(email)
                    .password(null) // No password for OAuth users
                    .authProvider(AuthProvider.GOOGLE)
                    .role(UserRole.USER)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);
        } else {
            // Update OAuth provider if different
            if (!user.getAuthProvider().equals(AuthProvider.GOOGLE)) {
                user.setAuthProvider(AuthProvider.GOOGLE);
                userRepository.save(user);
            }
        }

        // Return OAuth2User with email as principal
        return new CustomOAuth2User(oAuth2User, user.getEmail());
    }
}
