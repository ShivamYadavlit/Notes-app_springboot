package com.fred.notesapp.dto;

public class NoteRequest {
    private String title;
    private String content;
    
    // Constructors
    public NoteRequest() {}
    
    public NoteRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}