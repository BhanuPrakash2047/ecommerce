package com.snackecommerce.common.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "OAuth2 authentication failed";

        if (exception instanceof OAuth2AuthenticationException) {
            errorMessage = ((OAuth2AuthenticationException) exception).getError().getDescription();
        } else if (exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }

        // Redirect to frontend error page
        String frontendUrl = "http://localhost:5173";
        String redirectUrl = frontendUrl + "/oauth2/error?message=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
