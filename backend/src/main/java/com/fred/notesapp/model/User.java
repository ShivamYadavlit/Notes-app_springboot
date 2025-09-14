package com.fred.notesapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@CompoundIndex(def = "{'email': 1, 'tenantId': 1}", unique = true)
public class User {
    @Id
    private String id;
    
    @Indexed
    private String email;
    
    private String password;
    private String role; // "ADMIN" or "MEMBER"
    
    @Indexed
    private String tenantId;
    
    // Constructors
    public User() {}
    
    public User(String email, String password, String role, String tenantId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}