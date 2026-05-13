package com.rextart.sys.sistemainformativo.service;

import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username '{}'", username);
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("Authentication failed: user '{}' not found", username);
            return new UsernameNotFoundException("User '" + username + "' not found");
        });
        return map(user);
    }

    private UserDetails map(User from) {
        return new org.springframework.security.core.userdetails.User(
                from.getUsername(),
                from.getPasswordHash(),
                from.isActive(),
                true,
                true,
                true,
                AuthorityUtils.createAuthorityList("ROLE_" + from.getRole().name())
        );
    }
}
