package com.fred.notesapp.controller;

import com.fred.notesapp.model.Tenant;
import com.fred.notesapp.model.User;
import com.fred.notesapp.service.TenantService;
import com.fred.notesapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/tenants")
@CrossOrigin(origins = "*")
public class TenantController {
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private UserService userService;
    
    // Upgrade tenant plan
    @PostMapping("/{slug}/upgrade")
    public ResponseEntity<?> upgradeTenant(@PathVariable String slug, Principal principal) {
        // Find tenant by slug first
        Optional<Tenant> tenantOpt = tenantService.findBySlug(slug);
        if (!tenantOpt.isPresent()) {
            return ResponseEntity.status(404).body("Tenant not found");
        }
        
        Tenant tenant = tenantOpt.get();
        
        // Get user from principal using the tenant ID
        Optional<User> userOpt = userService.findByEmailAndTenantId(principal.getName(), tenant.getId());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        User user = userOpt.get();
        
        // Check if user is admin
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only admins can upgrade subscription");
        }
        
        // Check if tenant matches user's tenant
        if (!tenant.getId().equals(user.getTenantId())) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        // Upgrade plan
        tenant.setPlan("PRO");
        tenantService.save(tenant);
        
        return ResponseEntity.ok().body("Tenant upgraded to PRO plan successfully");
    }
}