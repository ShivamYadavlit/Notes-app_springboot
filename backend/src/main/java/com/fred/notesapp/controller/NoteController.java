package com.fred.notesapp.controller;

import com.fred.notesapp.dto.NoteRequest;
import com.fred.notesapp.dto.NoteResponse;
import com.fred.notesapp.model.Note;
import com.fred.notesapp.model.User;
import com.fred.notesapp.service.NoteService;
import com.fred.notesapp.service.TenantService;
import com.fred.notesapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notes")
@CrossOrigin(origins = "*")
public class NoteController {
    
    @Autowired
    private NoteService noteService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TenantService tenantService;
    
    // Create a note
    @PostMapping
    public ResponseEntity<?> createNote(@RequestBody NoteRequest noteRequest, Principal principal) {
        // Check if principal is null
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        
        // Get user from principal
        Optional<User> userOpt = userService.findByEmailAndTenantId(principal.getName(), getTenantIdFromEmail(principal.getName()));
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        
        User user = userOpt.get();
        
        // Check note limit based on user role
        if (noteService.isNoteLimitReached(user.getTenantId(), user.getId(), user.getRole())) {
            String limitMessage = "ADMIN".equals(user.getRole()) ? 
                "Admin note limit reached (2 notes). Upgrade to PRO plan for unlimited notes." :
                "User note limit reached (1 note). Contact your admin to upgrade to PRO plan.";
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(limitMessage);
        }
        
        // Create note
        Note note = new Note(
                noteRequest.getTitle(),
                noteRequest.getContent(),
                user.getTenantId(),
                user.getId()
        );
        
        Note savedNote = noteService.save(note);
        
        NoteResponse response = new NoteResponse(
                savedNote.getId(),
                savedNote.getTitle(),
                savedNote.getContent(),
                savedNote.getCreatedAt(),
                savedNote.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // List all notes for current tenant
    @GetMapping
    public ResponseEntity<?> getAllNotes(Principal principal) {
        // Check if principal is null
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        
        // Get user from principal
        Optional<User> userOpt = userService.findByEmailAndTenantId(principal.getName(), getTenantIdFromEmail(principal.getName()));
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        
        User user = userOpt.get();
        
        List<Note> notes = noteService.findByTenantIdAndUserId(user.getTenantId(), user.getId());
        
        List<NoteResponse> response = notes.stream().map(note -> 
                new NoteResponse(
                        note.getId(),
                        note.getTitle(),
                        note.getContent(),
                        note.getCreatedAt(),
                        note.getUpdatedAt()
                )
        ).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    // Get a specific note
    @GetMapping("/{id}")
    public ResponseEntity<?> getNote(@PathVariable String id, Principal principal) {
        // Check if principal is null
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        
        // Get user from principal
        Optional<User> userOpt = userService.findByEmailAndTenantId(principal.getName(), getTenantIdFromEmail(principal.getName()));
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        
        User user = userOpt.get();
        
        Optional<Note> noteOpt = noteService.findById(id);
        if (!noteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Note not found");
        }
        
        Note note = noteOpt.get();
        
        // Check if note belongs to user's tenant
        if (!note.getTenantId().equals(user.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        // Check if note belongs to user or user is admin
        if (!note.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        NoteResponse response = new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Update a note
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable String id, @RequestBody NoteRequest noteRequest, Principal principal) {
        // Check if principal is null
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        
        // Get user from principal
        Optional<User> userOpt = userService.findByEmailAndTenantId(principal.getName(), getTenantIdFromEmail(principal.getName()));
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        
        User user = userOpt.get();
        
        Optional<Note> noteOpt = noteService.findById(id);
        if (!noteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Note not found");
        }
        
        Note note = noteOpt.get();
        
        // Check if note belongs to user's tenant
        if (!note.getTenantId().equals(user.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        // Check if note belongs to user or user is admin
        if (!note.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        // Update note
        note.setTitle(noteRequest.getTitle());
        note.setContent(noteRequest.getContent());
        note.setUpdatedAt(LocalDateTime.now());
        
        Note updatedNote = noteService.update(note);
        
        NoteResponse response = new NoteResponse(
                updatedNote.getId(),
                updatedNote.getTitle(),
                updatedNote.getContent(),
                updatedNote.getCreatedAt(),
                updatedNote.getUpdatedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Delete a note
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable String id, Principal principal) {
        // Check if principal is null
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        
        // Get user from principal
        Optional<User> userOpt = userService.findByEmailAndTenantId(principal.getName(), getTenantIdFromEmail(principal.getName()));
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        
        User user = userOpt.get();
        
        Optional<Note> noteOpt = noteService.findById(id);
        if (!noteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Note not found");
        }
        
        Note note = noteOpt.get();
        
        // Check if note belongs to user's tenant
        if (!note.getTenantId().equals(user.getTenantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        // Check if note belongs to user or user is admin
        if (!note.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        noteService.deleteById(id);
        
        return ResponseEntity.ok().body("Note deleted successfully");
    }
    
    private String getTenantIdFromEmail(String email) {
        String[] emailParts = email.split("@");
        if (emailParts.length < 2) {
            return null;
        }
        String tenantSlug = emailParts[1].split("\\.")[0]; // Extract "acme" from "admin@acme.test"
        
        // Get tenant by slug and return its ID
        return tenantService.findBySlug(tenantSlug)
                .map(tenant -> tenant.getId())
                .orElse(null);
    }
}