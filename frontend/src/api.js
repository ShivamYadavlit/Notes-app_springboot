// API utility functions
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'https://notes-app-0nri.onrender.com';

export const api = {
  // Test API connection
  testConnection: async () => {
    try {
      const response = await fetch(`${BACKEND_URL}/health`);
      return response.ok;
    } catch (error) {
      console.error('API connection test failed:', error);
      return false;
    }
  },
  
  // Login endpoint
  login: async (email, password) => {
    const response = await fetch(`${BACKEND_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });
    
    if (!response.ok) {
      throw new Error(`Login failed with status ${response.status}`);
    }
    
    return response.json();
  },
  
  // Get notes for authenticated user
  getNotes: async (token) => {
    const response = await fetch(`${BACKEND_URL}/notes`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    
    if (!response.ok) {
      throw new Error(`Failed to fetch notes with status ${response.status}`);
    }
    
    return response.json();
  }
};