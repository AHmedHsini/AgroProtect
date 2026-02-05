package com.agriplatform.identity.security;

import com.agriplatform.identity.entity.User;
import com.agriplatform.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom UserDetailsService implementation.
 * Loads user by UUID (used as subject in JWT).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userUuid) throws UsernameNotFoundException {
        User user = userRepository.findActiveByUuidWithRoles(userUuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userUuid));

        var authorities = Stream.concat(
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())),
                user.getPermissionNames().stream()
                        .map(SimpleGrantedAuthority::new))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUuid(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                user.isActive(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isLocked(), // accountNonLocked
                authorities);
    }

    /**
     * Load user by email (for email/password authentication).
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findActiveByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        var authorities = Stream.concat(
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())),
                user.getPermissionNames().stream()
                        .map(SimpleGrantedAuthority::new))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUuid(), // Use UUID as username for consistency
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                user.isActive(),
                true,
                true,
                !user.isLocked(),
                authorities);
    }
}
