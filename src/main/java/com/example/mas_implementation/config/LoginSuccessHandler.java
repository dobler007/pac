package com.example.mas_implementation.config;

import com.example.mas_implementation.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * After Spring Security authenticates a user, this handler stores the user's database id
 * in the HTTP session under {@code currentUserId}. The MVC controllers resolve the active
 * user from that attribute, so authentication and the rest of the application stay decoupled.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        userRepository.findByLogin(authentication.getName())
                .ifPresent(user -> request.getSession().setAttribute("currentUserId", user.getId()));
        response.sendRedirect(request.getContextPath() + "/");
    }
}
