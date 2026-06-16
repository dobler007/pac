package com.example.mas_implementation.config;

import com.example.mas_implementation.model.IAdmin;
import com.example.mas_implementation.model.User;
import com.example.mas_implementation.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads a {@link User} by login for Spring Security and maps the domain type to roles.
 * Any user that implements {@link IAdmin} (Admin, PlayerAdmin) is granted ROLE_ADMIN
 * in addition to ROLE_USER; everyone else is a plain ROLE_USER.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("No user with login " + login));

        List<SimpleGrantedAuthority> authorities = (user instanceof IAdmin)
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getLogin())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
