package com.fred.notesapp.service;

import com.fred.notesapp.model.User;
import com.fred.notesapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Optional<User> findByEmailAndTenantId(String email, String tenantId) {
        return userRepository.findByEmailAndTenantId(email, tenantId);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User save(User user) {
        // Check if user with same email and tenant already exists
        Optional<User> existingUser = userRepository.findByEmailAndTenantId(user.getEmail(), user.getTenantId());
        if (existingUser.isPresent()) {
            // If exists, update the existing user instead of creating a new one
            User updatedUser = existingUser.get();
            updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
            updatedUser.setRole(user.getRole());
            return userRepository.save(updatedUser);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    public void deleteAll() {
        userRepository.deleteAll();
    }
}