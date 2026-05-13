package com.rextart.sys.sistemainformativo.service;

import com.rextart.sys.sistemainformativo.model.User;
import com.rextart.sys.sistemainformativo.repository.UserRepository;
import lombok.AllArgsConstructor;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
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
