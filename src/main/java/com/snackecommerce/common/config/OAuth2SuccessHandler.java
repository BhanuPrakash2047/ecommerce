package com.snackecommerce.common.config;

import com.snackecommerce.common.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            logger.debug("Response has already been committed");
            return;
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email;

        if (oAuth2User instanceof CustomOAuth2User) {
            email = ((CustomOAuth2User) oAuth2User).getEmail();
        } else {
            email = oAuth2User.getAttribute("email");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(email);

        // Redirect to frontend with token
        String redirectUrl = UriComponentsBuilder.fromUriString("/oauth2/success")
                .queryParam("token", token)
                .queryParam("email", email)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
