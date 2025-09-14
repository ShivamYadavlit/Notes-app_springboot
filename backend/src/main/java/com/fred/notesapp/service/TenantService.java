package com.fred.notesapp.service;

import com.fred.notesapp.model.Tenant;
import com.fred.notesapp.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService {
    
    @Autowired
    private TenantRepository tenantRepository;
    
    public Optional<Tenant> findBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }
    
    public Tenant save(Tenant tenant) {
        // Check if tenant with same slug already exists
        Optional<Tenant> existingTenant = tenantRepository.findBySlug(tenant.getSlug());
        if (existingTenant.isPresent()) {
            // If exists, update the existing tenant instead of creating a new one
            Tenant updatedTenant = existingTenant.get();
            updatedTenant.setName(tenant.getName());
            updatedTenant.setPlan(tenant.getPlan());
            return tenantRepository.save(updatedTenant);
        }
        return tenantRepository.save(tenant);
    }
    
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }
    
    public void deleteAll() {
        tenantRepository.deleteAll();
    }
}