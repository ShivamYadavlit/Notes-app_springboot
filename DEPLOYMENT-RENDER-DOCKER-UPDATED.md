# Deploying Spring Boot Backend to Render Using Docker (Updated)

This guide explains how to deploy the Notes Application Spring Boot backend to Render using Docker container deployment with the optimized Dockerfile.

## Prerequisites

1. A Render account (free or paid)
2. A MongoDB database (MongoDB Atlas recommended)
3. This repository forked or cloned
4. Docker installed locally (for testing)

## Deployment Steps

### 1. Create a MongoDB Database

If you don't already have a MongoDB database:

1. Sign up for [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create a new cluster
3. Configure network access to allow connections from Render (you may need to add `0.0.0.0/0` temporarily)
4. Create a database user with read/write permissions
5. Get your connection string in the format:
   ```
   mongodb+srv://<username>:<password>@<cluster-url>/<database-name>?retryWrites=true&w=majority
   ```

### 2. Prepare Your Docker Configuration

Your project now includes an optimized Dockerfile in the backend directory with multi-stage build:

```dockerfile
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src
COPY .env .env

# Install Maven and build the application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the jar file from build stage
COPY --from=build /app/target/notesapp-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3. Environment Variables Setup

The application uses environment variables for configuration. These can be set in multiple ways:

#### Using .env File (Local Development)
For local development, you can use the [.env](file:///C:/Users/shubh/OneDrive/Desktop/Fred/backend/.env) file in the backend directory:

```properties
# MongoDB Connection
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster0.jvk8bpk.mongodb.net/notesapp?retryWrites=true&w=majority

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here-at-least-32-characters
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080

# CORS Configuration
SPRING_WEB_CORS_ALLOWED_ORIGINS=*
SPRING_WEB_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
SPRING_WEB_CORS_ALLOWED_HEADERS=*
```

#### Using Render Environment Variables (Production)
For production deployment on Render, set environment variables in the Render dashboard.

### 4. Create a New Web Service on Render (Docker Method)

1. Log in to your Render account
2. Click "New" and select "Web Service"
3. Connect your repository or paste the repository URL
4. Configure the following settings:

#### Build Settings
- **Name**: notesapp-backend (or any name you prefer)
- **Region**: Choose the region closest to your users
- **Branch**: main (or your preferred branch)
- **Root Directory**: `/backend` (note the change to backend directory)
- **Environment**: Docker
- **Dockerfile Path**: `Dockerfile` (this tells Render to use your backend Dockerfile)

#### Environment Variables

Add the following environment variables in the Render dashboard (these will override the defaults in the Dockerfile):

| Variable Name | Value |
|---------------|-------|
| `SPRING_DATA_MONGODB_URI` | Your MongoDB connection string |
| `JWT_SECRET` | A strong secret key for JWT token generation |
| `JWT_EXPIRATION` | Token expiration time in milliseconds (e.g., 86400000 for 24 hours) |

Example MongoDB URI:
```
mongodb+srv://username:password@cluster0.jvk8bpk.mongodb.net/notesapp?retryWrites=true&w=majority
```

### 5. Configure Advanced Settings (Optional)

#### Health Check
Render can automatically check your application's health:
- **Health Check Path**: `/health`
- **Protocol**: HTTP
- **Port**: 8080

#### Auto-Deploy
Enable auto-deploy to automatically deploy new commits to your branch.

### 6. Deploy

1. Click "Create Web Service"
2. Render will automatically start building and deploying your application using Docker
3. Wait for the deployment to complete (this may take several minutes as Maven needs to download dependencies)

### 7. Initialize Test Data (Optional)

After deployment, you can initialize test data by making a POST request to your deployed application's `/init` endpoint:

```bash
curl -X POST https://your-app-name.onrender.com/init
```

Replace `your-app-name` with your actual Render app name.

## Environment Variables Reference

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SPRING_DATA_MONGODB_URI` | MongoDB connection string | Yes | - |
| `JWT_SECRET` | Secret key for JWT token signing | Yes | - |
| `JWT_EXPIRATION` | Token expiration time (ms) | No | 86400000 (24 hours) |
| `PORT` | Port to run the application on | No | 8080 |

## Troubleshooting

### Common Issues

1. **Build Failures**
   - Check the build logs in Render dashboard
   - Ensure Maven can download all dependencies
   - Verify the Dockerfile path is correct

2. **Database Connection Failed**
   - Check your MongoDB URI
   - Ensure your MongoDB cluster allows connections from Render IP addresses
   - Verify database username and password

3. **Application Crashes on Startup**
   - Check the logs in Render dashboard
   - Ensure all required environment variables are set
   - Verify the MongoDB connection

4. **Health Check Fails**
   - Ensure the `/health` endpoint is accessible
   - Check if the application is binding to the correct port

### Viewing Logs

You can view your application logs directly in the Render dashboard:
1. Go to your web service
2. Click on "Logs" tab
3. View real-time logs or historical logs

## Testing Your Docker Deployment Locally

Before deploying to Render, you can test your Docker configuration locally:

1. **Navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Build the Docker image:**
   ```bash
   docker build -t notesapp-backend .
   ```

3. **Run the container locally:**
   ```bash
   docker run -d \
     --name notesapp-backend \
     -p 8080:8080 \
     -e SPRING_DATA_MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/notesapp" \
     -e JWT_SECRET="your-jwt-secret-key" \
     notesapp-backend
   ```

4. **Test the application:**
   ```bash
   curl http://localhost:8080/health
   ```

5. **Stop the container:**
   ```bash
   docker stop notesapp-backend
   ```

## Scaling

Render automatically handles scaling for you. For production applications, consider:
- Using a paid Render plan for better performance
- Setting up custom domains
- Configuring SSL certificates (Render provides free SSL)
- Setting up monitoring and alerts

## Updating Your Application

To update your deployed application:
1. Push changes to your repository
2. If auto-deploy is enabled, Render will automatically deploy the changes
3. If auto-deploy is disabled, manually trigger a deploy in the Render dashboard

## Docker Best Practices for Render

### Optimizing Build Times

The optimized Dockerfile includes:

1. **Multi-stage Builds**: Separates build and runtime environments to reduce final image size
2. **Non-root User**: Security best practice to run the application as a non-root user
3. **Layer Caching**: Structure optimizes Docker layer caching for faster rebuilds

### Health Checks

Ensure your application has a proper health check endpoint:
```java
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }
}
```

## Additional Resources

- [Render Docker Documentation](https://render.com/docs/docker)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MongoDB Atlas Documentation](https://docs.atlas.mongodb.com/)

## Support

If you encounter any issues with deployment:
1. Check the Render logs for error messages
2. Verify all environment variables are correctly set
3. Ensure your MongoDB database is accessible
4. Contact Render support if the issue is related to the platform