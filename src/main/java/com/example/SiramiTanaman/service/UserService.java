package com.example.SiramiTanaman.service;

import com.example.SiramiTanaman.model.User;
import com.example.SiramiTanaman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User registerUser(User user) {
        return userRepository.save(user);
    }
}