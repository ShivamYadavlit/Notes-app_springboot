package com.fred.notesapp.dto;

public class SignupRequest {
    private String email;
    private String password;
    private String tenantName;
    
    // Constructors
    public SignupRequest() {}
    
    public SignupRequest(String email, String password, String tenantName) {
        this.email = email;
        this.password = password;
        this.tenantName = tenantName;
    }
    
    // Getters and Setters
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
    
    public String getTenantName() {
        return tenantName;
    }
    
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
}