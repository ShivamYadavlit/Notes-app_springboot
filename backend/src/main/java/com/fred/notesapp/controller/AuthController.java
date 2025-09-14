package com.fred.notesapp.controller;

import com.fred.notesapp.dto.LoginRequest;
import com.fred.notesapp.dto.LoginResponse;
import com.fred.notesapp.dto.SignupRequest;
import com.fred.notesapp.model.Tenant;
import com.fred.notesapp.model.User;
import com.fred.notesapp.security.JwtUtil;
import com.fred.notesapp.service.TenantService;
import com.fred.notesapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Test endpoint working");
    }
    
    @GetMapping("/test-users")
    public ResponseEntity<?> testUsers() {
        try {
            System.out.println("Retrieving all users");
            List<User> users = userService.findAll();
            System.out.println("Found " + users.size() + " users");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("Error retrieving users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error retrieving users: " + e.getMessage());
        }
    }
    
    @GetMapping("/test-tenants")
    public ResponseEntity<?> testTenants() {
        try {
            System.out.println("Retrieving all tenants");
            List<Tenant> tenants = tenantService.findAll();
            System.out.println("Found " + tenants.size() + " tenants");
            // Print detailed tenant information
            for (Tenant tenant : tenants) {
                System.out.println("Tenant ID: " + tenant.getId() + 
                                 ", Slug: " + tenant.getSlug() + 
                                 ", Name: " + tenant.getName() + 
                                 ", Plan: " + tenant.getPlan());
            }
            return ResponseEntity.ok(tenants);
        } catch (Exception e) {
            System.err.println("Error retrieving tenants: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error retrieving tenants: " + e.getMessage());
        }
    }
    
    // Signup endpoint
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            System.out.println("Signup request received for email: " + signupRequest.getEmail());
            
            // Validate input
            if (signupRequest.getEmail() == null || signupRequest.getEmail().isEmpty()) {
                System.out.println("Email is required");
                return ResponseEntity.badRequest().body("Email is required");
            }
            
            if (signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()) {
                System.out.println("Password is required");
                return ResponseEntity.badRequest().body("Password is required");
            }
            
            if (signupRequest.getTenantName() == null || signupRequest.getTenantName().isEmpty()) {
                System.out.println("Tenant name is required");
                return ResponseEntity.badRequest().body("Tenant name is required");
            }
            
            // Extract tenant from email (before @)
            String[] emailParts = signupRequest.getEmail().split("@");
            if (emailParts.length < 2) {
                System.out.println("Invalid email format");
                return ResponseEntity.badRequest().body("Invalid email format");
            }
            
            String tenantSlug = emailParts[1].split("\\.")[0]; // Extract "acme" from "acme.test"
            System.out.println("Tenant slug: " + tenantSlug);
            
            // Check if tenant already exists
            Optional<Tenant> existingTenant = tenantService.findBySlug(tenantSlug);
            Tenant tenant;
            
            if (existingTenant.isPresent()) {
                tenant = existingTenant.get();
                System.out.println("Using existing tenant: " + tenant.getName());
            } else {
                // Create new tenant
                tenant = new Tenant(tenantSlug, signupRequest.getTenantName(), "FREE");
                tenant = tenantService.save(tenant);
                System.out.println("Created new tenant: " + tenant.getName() + " with ID: " + tenant.getId());
            }
            
            // Check if user already exists
            Optional<User> existingUser = userService.findByEmailAndTenantId(signupRequest.getEmail(), tenant.getId());
            if (existingUser.isPresent()) {
                System.out.println("User already exists");
                return ResponseEntity.badRequest().body("User already exists");
            }
            
            // Create new user
            String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
            User user = new User(signupRequest.getEmail(), encodedPassword, "MEMBER", tenant.getId());
            user = userService.save(user);
            System.out.println("Created new user: " + user.getEmail() + " with ID: " + user.getId());
            
            // Generate JWT token
            System.out.println("Generating JWT token");
            final String jwt = jwtUtil.generateToken(user.getEmail());
            System.out.println("JWT token generated: " + jwt);
            
            LoginResponse response = new LoginResponse(
                    jwt,
                    user.getEmail(),
                    user.getRole(),
                    tenant.getSlug()
            );
            
            System.out.println("Signup successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Internal server error in signup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login request received for email: " + loginRequest.getEmail());
            
            // Validate input
            if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
                System.out.println("Email is required");
                return ResponseEntity.badRequest().body("Email is required");
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                System.out.println("Password is required");
                return ResponseEntity.badRequest().body("Password is required");
            }
            
            // Extract tenant from email (before @)
            String[] emailParts = loginRequest.getEmail().split("@");
            if (emailParts.length < 2) {
                System.out.println("Invalid email format");
                return ResponseEntity.badRequest().body("Invalid email format");
            }
            
            String tenantDomain = emailParts[1].split("\\.")[0]; // Extract "acme" from "acme.test"
            System.out.println("Tenant domain: " + tenantDomain);
            
            // Debug: Check all tenants
            System.out.println("All tenants in database:");
            tenantService.findAll().forEach(t -> System.out.println("  Tenant: ID=" + t.getId() + ", Slug=" + t.getSlug() + ", Name=" + t.getName()));
            
            Optional<Tenant> tenantOpt = tenantService.findBySlug(tenantDomain);
            
            if (!tenantOpt.isPresent()) {
                System.out.println("Tenant not found");
                return ResponseEntity.badRequest().body("Tenant not found");
            }
            
            System.out.println("Tenant found: " + tenantOpt.get().getName() + " with ID: " + tenantOpt.get().getId());
            
            Optional<User> userOpt = userService.findByEmailAndTenantId(loginRequest.getEmail(), tenantOpt.get().getId());
            
            if (!userOpt.isPresent()) {
                System.out.println("User not found for email: " + loginRequest.getEmail() + " and tenant ID: " + tenantOpt.get().getId());
                return ResponseEntity.badRequest().body("User not found");
            }
            
            User user = userOpt.get();
            System.out.println("User found: " + user.getEmail() + " with role: " + user.getRole() + " and tenant ID: " + user.getTenantId());
            
            // Check if password matches
            System.out.println("Checking password match");
            System.out.println("Plain password: " + loginRequest.getPassword());
            System.out.println("Encrypted password: " + user.getPassword());
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            System.out.println("Password matches: " + passwordMatches);
            
            if (!passwordMatches) {
                System.out.println("Invalid credentials");
                return ResponseEntity.status(401).body("Invalid credentials");
            }
            
            // Generate JWT token
            System.out.println("Generating JWT token");
            final String jwt = jwtUtil.generateToken(user.getEmail());
            System.out.println("JWT token generated: " + jwt);
            
            LoginResponse response = new LoginResponse(
                    jwt,
                    user.getEmail(),
                    user.getRole(),
                    tenantOpt.get().getSlug()
            );
            
            System.out.println("Login successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Internal server error in login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
    
    // Initialize test data
    @PostMapping("/init")
    public ResponseEntity<?> initializeTestData() {
        try {
            System.out.println("Initializing test data");
            
            // Delete existing data first
            System.out.println("Deleting existing users and tenants");
            userService.deleteAll();
            tenantService.deleteAll();
            
            // Create tenants
            Tenant acme = new Tenant("acme", "Acme Corp", "FREE");
            Tenant globex = new Tenant("globex", "Globex Inc", "FREE");
            
            acme = tenantService.save(acme);
            globex = tenantService.save(globex);
            
            System.out.println("Tenants created - Acme ID: " + acme.getId() + ", Globex ID: " + globex.getId());
            System.out.println("Acme name: " + acme.getName() + ", Globex name: " + globex.getName());
            
            // Create users with plain passwords (they will be encoded in UserService)
            User adminAcme = new User("admin@acme.test", "password", "ADMIN", acme.getId());
            User userAcme = new User("user@acme.test", "password", "MEMBER", acme.getId());
            User adminGlobex = new User("admin@globex.test", "password", "ADMIN", globex.getId());
            User userGlobex = new User("user@globex.test", "password", "MEMBER", globex.getId());
            
            userService.save(adminAcme);
            userService.save(userAcme);
            userService.save(adminGlobex);
            userService.save(userGlobex);
            
            System.out.println("Users created");
            
            return ResponseEntity.ok("Test data initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing test data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error initializing test data: " + e.getMessage());
        }
    }
}