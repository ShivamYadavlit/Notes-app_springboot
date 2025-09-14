package com.fred.notesapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        System.out.println("Processing request: " + request.getMethod() + " " + request.getRequestURI());
        
        // Skip authentication for login, init, health, and test endpoints
        // Also skip for OPTIONS requests (CORS preflight)
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        if (method.equals("OPTIONS") || 
            requestURI.equals("/login") || 
            requestURI.equals("/init") || 
            requestURI.equals("/health") || 
            requestURI.equals("/test") || 
            requestURI.equals("/test-users") ||
            requestURI.startsWith("/swagger") ||
            requestURI.startsWith("/v3/api-docs")) {
            System.out.println("Skipping authentication for: " + method + " " + requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String email = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwt);
                System.out.println("Extracted email from JWT: " + email);
            } catch (Exception e) {
                System.err.println("Error extracting email from JWT: " + e.getMessage());
                System.err.println("JWT token: " + jwt);
                e.printStackTrace();
            }
        }
        
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
                System.out.println("Loaded user details for: " + email);
                
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    System.out.println("Authentication set for user: " + email);
                } else {
                    System.out.println("JWT token validation failed for user: " + email);
                }
            } catch (Exception e) {
                System.err.println("Error loading user details: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (email == null && authorizationHeader != null) {
            System.out.println("No email extracted from JWT, authorization header: " + authorizationHeader);
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            System.out.println("Authentication already exists in context");
        }
        
        chain.doFilter(request, response);
    }
}