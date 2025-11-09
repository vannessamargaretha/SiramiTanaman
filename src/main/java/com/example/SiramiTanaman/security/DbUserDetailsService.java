package com.example.SiramiTanaman.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.SiramiTanaman.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        var u = userRepository.findByUsername(input)
                .or(() -> userRepository.findByEmail(input))  // try email too
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + input));

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),  // Spring Security uses this internally
                u.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}