import { useState, useEffect } from 'react';
import './App.css';

// Use environment variable for backend URL, with fallback to deployed backend
const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || 'https://notes-app-0nri.onrender.com';

function App() {
  const [user, setUser] = useState(null);
  const [notes, setNotes] = useState([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [editingNoteId, setEditingNoteId] = useState(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [tenantName, setTenantName] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLogin, setIsLogin] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [activeSection, setActiveSection] = useState('dashboard');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // Check if user is already logged in
  useEffect(() => {
    // Clear any existing tokens on app load to prevent signature mismatch
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    
    // If we have a token, try to validate it
    if (token && userData) {
      setUser(JSON.parse(userData));
      fetchNotes(token);
    }
  }, []);

  const fetchNotes = async (token) => {
    try {
      console.log('Fetching notes with token:', token ? 'Token exists' : 'No token');
      
      const response = await fetch(`${BACKEND_URL}/notes`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      console.log('Notes response status:', response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log('Notes fetched:', data);
        setNotes(data);
      } else if (response.status === 401 || response.status === 403) {
        // Token is invalid, clear it and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        setError('Session expired. Please log in again.');
      } else {
        const errorText = await response.text();
        console.log('Notes fetch error:', errorText);
        throw new Error('Failed to fetch notes: ' + errorText);
      }
    } catch (err) {
      console.error('Error fetching notes:', err);
      setError('Failed to fetch notes. Please try again.');
    }
  };

  const fetchNoteCount = () => {
    return notes.length;
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);
    
    try {
      console.log('Attempting login with:', { email, password });
      
      const response = await fetch(`${BACKEND_URL}/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: email,
          password: password
        })
      });
      
      console.log('Login response status:', response.status);
      console.log('Login response ok:', response.ok);
      
      // Always read the response body
      const responseText = await response.text();
      console.log('Login raw response text:', responseText);
      
      if (response.ok) {
        try {
          const data = JSON.parse(responseText);
          console.log('Login successful, received data:', data);
          localStorage.setItem('token', data.token);
          localStorage.setItem('user', JSON.stringify({
            email: data.email,
            role: data.role,
            tenantSlug: data.tenantSlug
          }));
          setUser({
            email: data.email,
            role: data.role,
            tenantSlug: data.tenantSlug
          });
          fetchNotes(data.token);
          setSuccess('Login successful!');
          setActiveSection('dashboard');
          // Clear form fields
          setEmail('');
          setPassword('');
        } catch (parseErr) {
          console.error('Error parsing login response JSON:', parseErr);
          setError('Received invalid response from server during login');
        }
      } else {
        try {
          const errorData = JSON.parse(responseText);
          setError(errorData.message || `Login failed (${response.status})`);
        } catch (parseErr) {
          console.error('Error parsing login error response:', parseErr);
          setError(`Login failed (${response.status}): ${responseText}`);
        }
      }
    } catch (err) {
      console.error('Login error:', err);
      // More detailed error message
      if (err instanceof TypeError && err.message.includes('fetch')) {
        setError(`Network error. Please check your connection and make sure the backend server is running at ${BACKEND_URL}`);
      } else {
        setError(`An error occurred: ${err.message}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSignup = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);
    
    try {
      const response = await fetch(`${BACKEND_URL}/signup`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: email,
          password: password,
          tenantName: tenantName
        })
      });
      
      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
          email: data.email,
          role: data.role,
          tenantSlug: data.tenantSlug
        }));
        setUser({
          email: data.email,
          role: data.role,
          tenantSlug: data.tenantSlug
        });
        fetchNotes(data.token);
        setSuccess('Signup successful! Welcome to Notes App.');
        setActiveSection('dashboard');
        // Clear form fields
        setEmail('');
        setPassword('');
        setTenantName('');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Signup failed');
      }
    } catch (err) {
      setError(`Network error. Please check your connection and make sure the backend server is running at ${BACKEND_URL}`);
      console.error('Signup error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setNotes([]);
    setEmail('');
    setPassword('');
    setTenantName('');
    setError('');
    setSuccess('');
    setTitle('');
    setContent('');
    setEditingNoteId(null);
    setActiveSection('dashboard');
    setIsMobileMenuOpen(false);
  };

  const handleCreateNote = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);
    
    try {
      const token = localStorage.getItem('token');
      
      // Debugging: Check if token exists
      if (!token) {
        throw new Error('No authentication token found. Please log in again.');
      }
      
      // Debugging: Log request details
      console.log('=== Creating note ===');
      console.log('Title:', title);
      console.log('Content:', content);
      console.log('Token length:', token.length);
      console.log('Token preview:', token.substring(0, 50) + '...');
      
      const requestBody = JSON.stringify({ title, content });
      console.log('Request body:', requestBody);
      
      console.log(`Making fetch request to ${BACKEND_URL}/notes`);
      
      const response = await fetch(`${BACKEND_URL}/notes`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: requestBody
      });
      
      console.log('=== Response received ===');
      console.log('Response status:', response.status);
      console.log('Response ok:', response.ok);
      console.log('Response headers:', [...response.headers.entries()]);
      
      // Always try to read the response body, even for error responses
      const responseText = await response.text();
      console.log('Raw response text:', responseText);
      
      if (response.ok) {
        // Try to parse JSON, but handle if it's not valid JSON
        try {
          const newNote = JSON.parse(responseText);
          console.log('Parsed note:', newNote);
          setNotes([...notes, newNote]);
          setTitle('');
          setContent('');
          setSuccess('Note created successfully!');
          setActiveSection('notes');
        } catch (parseErr) {
          console.error('Error parsing response JSON:', parseErr);
          setError('Received invalid response from server');
        }
      } else if (response.status === 401 || response.status === 403) {
        // Token is invalid, clear it and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        setError('Session expired. Please log in again.');
      } else {
        // Handle error response
        try {
          const errorData = JSON.parse(responseText);
          console.log('Parsed error data:', errorData);
          setError(errorData.message || `Failed to create note (${response.status})`);
        } catch (parseErr) {
          console.error('Error parsing error response:', parseErr);
          setError(`Failed to create note (${response.status}): ${responseText}`);
        }
      }
    } catch (err) {
      console.error('=== Network error occurred ===');
      console.error('Error name:', err.name);
      console.error('Error message:', err.message);
      console.error('Error stack:', err.stack);
      
      // More detailed error message
      if (err instanceof TypeError && err.message.includes('fetch')) {
        setError(`Network error. Please check your connection and make sure the backend server is running at ${BACKEND_URL}`);
      } else {
        setError(`An error occurred: ${err.message}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateNote = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);
    
    try {
      const token = localStorage.getItem('token');
      
      // Debugging: Check if token exists
      if (!token) {
        throw new Error('No authentication token found. Please log in again.');
      }
      
      // Debugging: Log request details
      console.log('=== Updating note ===');
      console.log('Note ID:', editingNoteId);
      console.log('Title:', title);
      console.log('Content:', content);
      console.log('Token length:', token.length);
      console.log('Token preview:', token.substring(0, 50) + '...');
      
      const requestBody = JSON.stringify({ title, content });
      console.log('Request body:', requestBody);
      
      console.log(`Making fetch request to ${BACKEND_URL}/notes/${editingNoteId}`);
      
      const response = await fetch(`${BACKEND_URL}/notes/${editingNoteId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: requestBody
      });
      
      console.log('=== Response received ===');
      console.log('Response status:', response.status);
      console.log('Response ok:', response.ok);
      console.log('Response headers:', [...response.headers.entries()]);
      
      // Always try to read the response body, even for error responses
      const responseText = await response.text();
      console.log('Raw response text:', responseText);
      
      if (response.ok) {
        // Try to parse JSON, but handle if it's not valid JSON
        try {
          const updatedNote = JSON.parse(responseText);
          console.log('Parsed note:', updatedNote);
          
          // Update the note in the state
          setNotes(notes.map(note => 
            note.id === editingNoteId ? updatedNote : note
          ));
          
          // Reset form and exit edit mode
          setTitle('');
          setContent('');
          setEditingNoteId(null);
          setSuccess('Note updated successfully!');
          setActiveSection('notes');
        } catch (parseErr) {
          console.error('Error parsing response JSON:', parseErr);
          setError('Received invalid response from server');
        }
      } else if (response.status === 401 || response.status === 403) {
        // Token is invalid, clear it and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        setError('Session expired. Please log in again.');
      } else {
        // Handle error response
        try {
          const errorData = JSON.parse(responseText);
          console.log('Parsed error data:', errorData);
          setError(errorData.message || `Failed to update note (${response.status})`);
        } catch (parseErr) {
          console.error('Error parsing error response:', parseErr);
          setError(`Failed to update note (${response.status}): ${responseText}`);
        }
      }
    } catch (err) {
      console.error('=== Network error occurred ===');
      console.error('Error name:', err.name);
      console.error('Error message:', err.message);
      console.error('Error stack:', err.stack);
      
      // More detailed error message
      if (err instanceof TypeError && err.message.includes('fetch')) {
        setError(`Network error. Please check your connection and make sure the backend server is running at ${BACKEND_URL}`);
      } else {
        setError(`An error occurred: ${err.message}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleEditNote = (note) => {
    setTitle(note.title);
    setContent(note.content);
    setEditingNoteId(note.id);
    setActiveSection('create');
    setIsMobileMenuOpen(false);
    // Scroll to the form
    setTimeout(() => {
      document.querySelector('.form-container').scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const handleCancelEdit = () => {
    setTitle('');
    setContent('');
    setEditingNoteId(null);
    setActiveSection('notes');
  };

  const handleDeleteNote = async (id) => {
    if (!window.confirm('Are you sure you want to delete this note?')) {
      return;
    }
    
    setError('');
    setSuccess('');
    setIsLoading(true);
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${BACKEND_URL}/notes/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        setNotes(notes.filter(note => note.id !== id));
        setSuccess('Note deleted successfully!');
      } else if (response.status === 401 || response.status === 403) {
        // Token is invalid, clear it and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        setError('Session expired. Please log in again.');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to delete note');
      }
    } catch (err) {
      setError(`Network error. Please check your connection and make sure the backend server is running at ${BACKEND_URL}`);
      console.error('Delete note error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpgrade = async () => {
    if (!window.confirm('Are you sure you want to upgrade to PRO plan?')) {
      return;
    }
    
    setError('');
    setSuccess('');
    setIsLoading(true);
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${BACKEND_URL}/tenants/${user.tenantSlug}/upgrade`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        setSuccess('Successfully upgraded to PRO plan!');
        // Refresh user data
        const updatedUser = { ...user, plan: 'PRO' };
        setUser(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
        // Refresh notes
        fetchNotes(token);
      } else if (response.status === 401 || response.status === 403) {
        // Token is invalid, clear it and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        setError('Session expired. Please log in again.');
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to upgrade');
      }
    } catch (err) {
      setError(`Network error. Please check your connection and make sure the backend server is running at ${BACKEND_URL}`);
      console.error('Upgrade error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  if (!user) {
    return (
      <div className="login-container">
        <div className="login-card">
          <h1>Notes App</h1>
          {error && <div className="error">{error}</div>}
          {success && <div className="success">{success}</div>}
          
          {isLogin ? (
            <div className="form-container-login">
              <h2>Login to Your Account</h2>
              <form onSubmit={handleLogin}>
                <div className="form-group">
                  <label htmlFor="login-email">Email:</label>
                  <input
                    id="login-email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={isLoading}
                    placeholder="Enter your email"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="login-password">Password:</label>
                  <input
                    id="login-password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    disabled={isLoading}
                    placeholder="Enter your password"
                  />
                </div>
                <button type="submit" disabled={isLoading}>
                  {isLoading ? 'Logging in...' : 'Login'}
                </button>
              </form>
            </div>
          ) : (
            <div className="form-container-login">
              <h2>Create New Account</h2>
              <form onSubmit={handleSignup}>
                <div className="form-group">
                  <label htmlFor="signup-email">Email:</label>
                  <input
                    id="signup-email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={isLoading}
                    placeholder="Enter your email"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="signup-password">Password:</label>
                  <input
                    id="signup-password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    disabled={isLoading}
                    placeholder="Create a password"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="tenant-name">Tenant Name:</label>
                  <input
                    id="tenant-name"
                    type="text"
                    value={tenantName}
                    onChange={(e) => setTenantName(e.target.value)}
                    required
                    disabled={isLoading}
                    placeholder="e.g., My Company"
                  />
                </div>
                <button type="submit" disabled={isLoading}>
                  {isLoading ? 'Signing up...' : 'Sign Up'}
                </button>
              </form>
            </div>
          )}
          
          <div 
            className="switch-form" 
            onClick={() => {
              setIsLogin(!isLogin);
              setError('');
              setSuccess('');
            }}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                setIsLogin(!isLogin);
                setError('');
                setSuccess('');
              }
            }}
          >
            {isLogin ? "Don't have an account? Sign Up" : "Already have an account? Login"}
          </div>
          
          {isLogin && (
            <div className="test-accounts">
              <h3>Test Accounts:</h3>
              <p>admin@acme.test (Admin, tenant: Acme) - password: password</p>
              <p>user@acme.test (Member, tenant: Acme) - password: password</p>
              <p>admin@globex.test (Admin, tenant: Globex) - password: password</p>
              <p>user@globex.test (Member, tenant: Globex) - password: password</p>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="app-container">
      {/* Mobile menu button */}
      <div className="mobile-menu-button" onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}>
        ☰
      </div>

      {/* Sidebar */}
      <div className={`sidebar ${isMobileMenuOpen ? 'mobile-open' : ''}`}>
        <div className="sidebar-header">
          <h2>Notes App</h2>
        </div>
        <ul className="sidebar-menu">
          <li>
            <a 
              href="#" 
              className={activeSection === 'dashboard' ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault();
                setActiveSection('dashboard');
                setIsMobileMenuOpen(false);
              }}
            >
              <i>📊</i> Dashboard
            </a>
          </li>
          <li>
            <a 
              href="#" 
              className={activeSection === 'notes' ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault();
                setActiveSection('notes');
                setIsMobileMenuOpen(false);
              }}
            >
              <i>📝</i> My Notes
            </a>
          </li>
          <li>
            <a 
              href="#" 
              className={activeSection === 'create' ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault();
                setTitle('');
                setContent('');
                setEditingNoteId(null);
                setActiveSection('create');
                setIsMobileMenuOpen(false);
              }}
            >
              <i>➕</i> {editingNoteId ? 'Edit Note' : 'Create Note'}
            </a>
          </li>
          {user.role === 'ADMIN' && (
            <li>
              <a 
                href="#" 
                className={activeSection === 'upgrade' ? 'active' : ''}
                onClick={(e) => {
                  e.preventDefault();
                  setActiveSection('upgrade');
                  setIsMobileMenuOpen(false);
                }}
              >
                <i>🚀</i> Upgrade Plan
              </a>
            </li>
          )}
        </ul>
      </div>

      {/* Main Content */}
      <div className="main-content">
        {/* Top Navigation */}
        <div className="top-nav">
          <div></div> {/* Spacer */}
          <div className="user-info">
            <span>Welcome, {user.email} ({user.role})</span>
            <button onClick={handleLogout} disabled={isLoading}>
              {isLoading ? 'Logging out...' : 'Logout'}
            </button>
          </div>
        </div>

        {/* Content Area */}
        <div className="content">
          {(error || success) && (
            <div>
              {error && <div className="error">{error}</div>}
              {success && <div className="success">{success}</div>}
            </div>
          )}

          {/* Dashboard Section */}
          {activeSection === 'dashboard' && (
            <div>
              <div className="dashboard-grid">
                <div className="dashboard-card">
                  <h3><i>📝</i> Total Notes</h3>
                  <div className="card-value">{fetchNoteCount()}</div>
                  <p>Notes you've created</p>
                </div>
                <div className="dashboard-card">
                  <h3><i>🏢</i> Tenant</h3>
                  <div className="card-value">{user.tenantSlug?.toUpperCase() || 'N/A'}</div>
                  <p>Your organization</p>
                </div>
                <div className="dashboard-card">
                  <h3><i>👑</i> Role</h3>
                  <div className="card-value">{user.role || 'Member'}</div>
                  <p>Your access level</p>
                </div>
                {user.role === 'ADMIN' && (
                  <div className="dashboard-card">
                    <h3><i>💎</i> Plan</h3>
                    <div className="card-value">FREE</div>
                    <p>Upgrade for unlimited notes</p>
                    <button onClick={() => setActiveSection('upgrade')}>
                      Upgrade Plan
                    </button>
                  </div>
                )}
              </div>

              <div className="section">
                <div className="section-header">
                  <h2>Recent Notes</h2>
                  <button onClick={() => setActiveSection('notes')}>
                    View All Notes
                  </button>
                </div>
                {notes.length === 0 ? (
                  <p>No notes yet. Create your first note!</p>
                ) : (
                  <div className="notes-grid">
                    {notes.slice(0, 3).map(note => (
                      <div key={note.id} className="note-card">
                        <h3>{note.title}</h3>
                        <p>{note.content}</p>
                        <small>Created: {new Date(note.createdAt).toLocaleDateString()}</small>
                        <div className="note-actions">
                          <button 
                            onClick={() => handleEditNote(note)} 
                            disabled={isLoading}
                          >
                            Edit
                          </button>
                          <button 
                            onClick={() => handleDeleteNote(note.id)} 
                            disabled={isLoading}
                            className="secondary-button"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Notes Section */}
          {activeSection === 'notes' && (
            <div className="section">
              <div className="section-header">
                <h2>Your Notes</h2>
                <button onClick={() => {
                  setTitle('');
                  setContent('');
                  setEditingNoteId(null);
                  setActiveSection('create');
                }}>
                  Create New Note
                </button>
              </div>
              {notes.length === 0 ? (
                <p>No notes yet. Create your first note!</p>
              ) : (
                <div className="notes-grid">
                  {notes.map(note => (
                    <div key={note.id} className="note-card">
                      <h3>{note.title}</h3>
                      <p>{note.content}</p>
                      <small>Created: {new Date(note.createdAt).toLocaleString()}</small>
                      <div className="note-actions">
                        <button 
                          onClick={() => handleEditNote(note)} 
                          disabled={isLoading}
                        >
                          Edit
                        </button>
                        <button 
                          onClick={() => handleDeleteNote(note.id)} 
                          disabled={isLoading}
                          className="secondary-button"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Create/Edit Note Section */}
          {activeSection === 'create' && (
            <div className="form-container">
              <h2>{editingNoteId ? 'Edit Note' : 'Create New Note'}</h2>
              {notes.length >= 3 && user.tenantSlug && !editingNoteId && (
                <div className="note-limit-warning">
                  <p>You've reached the note limit for the FREE plan.</p>
                  {user.role === 'ADMIN' ? (
                    <button 
                      onClick={() => setActiveSection('upgrade')}
                    >
                      Upgrade to PRO Plan
                    </button>
                  ) : (
                    <p>Contact your admin to upgrade to PRO plan.</p>
                  )}
                </div>
              )}
              <form onSubmit={editingNoteId ? handleUpdateNote : handleCreateNote}>
                <div className="form-group">
                  <label htmlFor="note-title">Title:</label>
                  <input
                    id="note-title"
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                    disabled={isLoading || (notes.length >= 3 && user.tenantSlug && !editingNoteId)}
                    placeholder="Enter note title"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="note-content">Content:</label>
                  <textarea
                    id="note-content"
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    required
                    disabled={isLoading || (notes.length >= 3 && user.tenantSlug && !editingNoteId)}
                    rows="5"
                    placeholder="Enter note content"
                  />
                </div>
                <div className="form-actions">
                  {editingNoteId ? (
                    <>
                      <button 
                        type="submit" 
                        disabled={isLoading}
                      >
                        {isLoading ? 'Updating...' : 'Update Note'}
                      </button>
                      <button 
                        type="button" 
                        onClick={handleCancelEdit}
                        disabled={isLoading}
                        className="secondary-button"
                      >
                        {isLoading ? 'Cancelling...' : 'Cancel'}
                      </button>
                    </>
                  ) : (
                    <button 
                      type="submit" 
                      disabled={isLoading || (notes.length >= 3 && user.tenantSlug)}
                    >
                      {isLoading ? 'Creating...' : 'Create Note'}
                    </button>
                  )}
                </div>
              </form>
            </div>
          )}

          {/* Upgrade Section */}
          {activeSection === 'upgrade' && user.role === 'ADMIN' && (
            <div className="section">
              <h2>Upgrade Your Plan</h2>
              <div className="dashboard-grid">
                <div className="dashboard-card">
                  <h3><i>💎</i> FREE Plan</h3>
                  <div className="card-value">3 Notes</div>
                  <p>Current plan</p>
                  <ul>
                    <li>✓ Up to 3 notes</li>
                    <li>✓ Basic features</li>
                    <li>✗ Unlimited notes</li>
                    <li>✗ Premium support</li>
                  </ul>
                </div>
                <div className="dashboard-card">
                  <h3><i>🚀</i> PRO Plan</h3>
                  <div className="card-value">∞ Notes</div>
                  <p>Upgrade now</p>
                  <ul>
                    <li>✓ Unlimited notes</li>
                    <li>✓ All basic features</li>
                    <li>✓ Premium support</li>
                    <li>✓ Advanced features</li>
                  </ul>
                  <button 
                    onClick={handleUpgrade} 
                    disabled={isLoading}
                  >
                    {isLoading ? 'Upgrading...' : 'Upgrade to PRO Plan - $9.99/month'}
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;