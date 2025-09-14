package com.fred.notesapp.service;

import com.fred.notesapp.model.Note;
import com.fred.notesapp.model.Tenant;
import com.fred.notesapp.model.User;
import com.fred.notesapp.repository.NoteRepository;
import com.fred.notesapp.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    public List<Note> findByTenantId(String tenantId) {
        return noteRepository.findByTenantId(tenantId);
    }
    
    public List<Note> findByTenantIdAndUserId(String tenantId, String userId) {
        return noteRepository.findByTenantIdAndUserId(tenantId, userId);
    }
    
    public Optional<Note> findById(String id) {
        return noteRepository.findById(id);
    }
    
    public Note save(Note note) {
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }
    
    public Note update(Note note) {
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }
    
    public void deleteById(String id) {
        noteRepository.deleteById(id);
    }
    
    public long countByTenantId(String tenantId) {
        return noteRepository.findByTenantId(tenantId).size();
    }
    
    public long countByTenantIdAndUserId(String tenantId, String userId) {
        return noteRepository.findByTenantIdAndUserId(tenantId, userId).size();
    }
    
    public boolean isNoteLimitReached(String tenantId, String userId, String userRole) {
        Optional<Tenant> tenant = tenantRepository.findBySlug(tenantId);
        if (tenant.isPresent() && "PRO".equals(tenant.get().getPlan())) {
            return false; // No limit for PRO plan
        }
        
        // Role-based limits for FREE plan
        long userNoteCount = countByTenantIdAndUserId(tenantId, userId);
        if ("ADMIN".equals(userRole)) {
            return userNoteCount >= 2; // Admin limit: 2 notes
        } else {
            return userNoteCount >= 1; // User limit: 1 note
        }
    }
}