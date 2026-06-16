package com.example.mas_implementation.web;

import com.example.mas_implementation.model.Player;
import com.example.mas_implementation.service.UserService;
import com.example.mas_implementation.web.dto.RegisterForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * Handles registration and the login view. Authentication of the login form and logout are
 * managed by Spring Security (see {@code SecurityConfig}); this controller only renders the
 * login page and creates new accounts.
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ── Register ─────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session) {

        // Bean Validation errors (blank fields, weak password, bad email, etc.)
        if (result.hasErrors()) {
            return "register";
        }

        // Username already taken
        if (userService.isLoginTaken(form.getLogin())) {
            result.rejectValue("login", "duplicate", "Username is already taken");
            return "register";
        }

        Player player = userService.register(form);

        // Auto-login: establish a Spring Security context and the MVC session attribute.
        authenticate(request, response, session, player);

        return "redirect:/";
    }

    // ── Login view (POST is processed by Spring Security) ─────────────────────

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, Player player) {
        UsernamePasswordAuthenticationToken auth = UsernamePasswordAuthenticationToken.authenticated(
                player.getLogin(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        session.setAttribute("currentUserId", player.getId());
    }
}
