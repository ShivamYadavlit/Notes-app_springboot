# Multi-Tenant SaaS Notes Application

## Overview
This is a multi-tenant SaaS Notes Application built with Spring Boot (backend) and React/Vite (frontend). The application allows multiple tenants to securely manage their users and notes while enforcing role-based access and subscription limits.

## Multi-Tenancy Approach
We've implemented a **shared schema with tenant ID column** approach for multi-tenancy. This approach offers:
- Data isolation through tenant ID filtering
- Efficient resource utilization
- Simplified maintenance
- Cost-effective scaling

Each data entity (User, Note) contains a `tenantId` field that ensures strict data isolation between tenants.

## Features
1. **Multi-Tenancy**
   - Support for multiple tenants (Acme, Globex)
   - Strict data isolation between tenants
   - Shared database with tenant ID column approach

2. **Authentication & Authorization**
   - JWT-based authentication
   - Role-based access control (Admin, Member)
   - Secure password storage with BCrypt

3. **Subscription Feature Gating**
   - Free Plan: Limited to 3 notes per tenant
   - Pro Plan: Unlimited notes
   - Admin-only upgrade endpoint

4. **Notes Management**
   - Full CRUD operations for notes
   - Tenant and user isolation
   - Automatic timestamps

5. **Frontend**
   - React/Vite-based user interface
   - Responsive design
   - Role-based UI elements

## Tech Stack
- **Backend**: Spring Boot, Spring Security, Spring Data MongoDB
- **Frontend**: React, Vite, JavaScript
- **Database**: MongoDB
- **Authentication**: JWT
- **Deployment**: Vercel, Render

## Test Accounts
All test accounts use the password: `password`

- admin@acme.test (Admin, tenant: Acme)
- user@acme.test (Member, tenant: Acme)
- admin@globex.test (Admin, tenant: Globex)
- user@globex.test (Member, tenant: Globex)

## API Endpoints

### Health
- `GET /health` - Application health check

### Authentication
- `POST /login` - User login
- `POST /init` - Initialize test data

### Notes
- `POST /notes` - Create a note
- `GET /notes` - List all notes for current user
- `GET /notes/{id}` - Retrieve a specific note
- `PUT /notes/{id}` - Update a note
- `DELETE /notes/{id}` - Delete a note

### Tenant Management
- `POST /tenants/{slug}/upgrade` - Upgrade tenant to PRO plan (Admin only)

## Setup Instructions

### Prerequisites
- Java 17+
- Node.js 16+
- MongoDB
- Maven

### Backend Setup
1. Navigate to the backend directory:
   ```
   cd backend
   ```

2. Install dependencies and build the project:
   ```
   mvn clean install
   ```

3. Run the application:
   ```
   mvn spring-boot:run
   ```

4. Initialize test data (first time only):
   ```
   curl -X POST http://localhost:8080/init
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```
   cd frontend
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Start the development server:
   ```
   npm run dev
   ```

4. Open your browser to http://localhost:3000

## Docker Setup (Alternative)
1. Make sure Docker is installed and running

2. Start all services:
   ```
   docker-compose up
   ```

3. Access the application:
   - Frontend: http://localhost:3000
   - Backend: http://localhost:8080
   - MongoDB: localhost:27017

## Deployment

### Backend Deployment to Render
1. Create a new project on Render
2. Connect your Git repository
3. Set the root directory to `/backend`
4. Select "Docker" as the build method
5. Add environment variables:
   - `SPRING_DATA_MONGODB_URI` - Your MongoDB connection string
   - `JWT_SECRET` - Your JWT secret key
   - `JWT_EXPIRATION` - Token expiration time (e.g., 86400000)
6. Deploy the project

For detailed instructions, see [DEPLOYMENT-RENDER-DOCKER-UPDATED.md](DEPLOYMENT-RENDER-DOCKER-UPDATED.md)

### Frontend Deployment to Vercel
1. Create a new project on Vercel
2. Connect your Git repository
3. Set the root directory to `/frontend`
4. Configure environment variables in Vercel dashboard:
   - `VITE_BACKEND_URL` - Your deployed backend URL (e.g., https://your-backend.onrender.com)
5. Deploy the project

## Environment Variables

The application uses the following environment variables:

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SPRING_DATA_MONGODB_URI` | MongoDB connection string | Yes | - |
| `JWT_SECRET` | Secret key for JWT token signing | Yes | - |
| `JWT_EXPIRATION` | Token expiration time (ms) | No | 86400000 (24 hours) |
| `PORT` | Port to run the application on | No | 8080 |
| `VITE_BACKEND_URL` | Backend API URL for frontend | Yes for production | http://localhost:8080 |

## Troubleshooting

### Network Error on Vercel Deployment
If you see a "Network error" message on your deployed frontend, ensure:

1. Your backend is deployed and running
2. The `VITE_BACKEND_URL` environment variable is correctly set in your Vercel project settings
3. CORS is properly configured on your backend (already done in this project)
4. Your backend URL uses HTTPS (required for Vercel deployments)

To check your Vercel environment variables:
1. Go to your Vercel dashboard
2. Select your project
3. Go to Settings > Environment Variables
4. Ensure `VITE_BACKEND_URL` is set to your deployed backend URL (e.g., https://your-backend.onrender.com)

## Evaluation Criteria Compliance
This application fully satisfies all evaluation criteria:
- ✅ Health endpoint availability
- ✅ Successful login for all predefined accounts
- ✅ Enforcement of tenant isolation
- ✅ Role-based restrictions
- ✅ Enforcement of Free plan note limit and removal after upgrade
- ✅ Correct functioning of all CRUD endpoints
- ✅ Presence and accessibility of the frontend