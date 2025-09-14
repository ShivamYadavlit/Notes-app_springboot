package com.fred.notesapp.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private String tenantSlug;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String token, String email, String role, String tenantSlug) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.tenantSlug = tenantSlug;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getTenantSlug() {
        return tenantSlug;
    }
    
    public void setTenantSlug(String tenantSlug) {
        this.tenantSlug = tenantSlug;
    }
}