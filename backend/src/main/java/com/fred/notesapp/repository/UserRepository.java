package com.fred.notesapp.repository;

import com.fred.notesapp.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmailAndTenantId(String email, String tenantId);
    Optional<User> findByEmail(String email);
    void deleteAll();
}