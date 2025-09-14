# Deploying Spring Boot Backend to Render Using Docker

This guide explains how to deploy the Notes Application Spring Boot backend to Render using Docker container deployment.

## Prerequisites

1. A Render account (free or paid)
2. This repository forked or cloned
3. Docker installed locally (for testing)

## Deployment Steps

### 1. Prepare Your Docker Configuration

Your project now includes a [Dockerfile](backend/Dockerfile) in the backend directory:

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven and build the application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package

# Expose port
EXPOSE 8080

# Environment variables with default values
ENV SPRING_DATA_MONGODB_URI=mongodb+srv://shivamyadav2072000_db_user:59wr5wd3V0HHAa0Y@cluster0.jvk8bpk.mongodb.net/notesapp?retryWrites=true&w=majority
ENV JWT_SECRET=NotesAppSecretKey12345678901234567890123456789012345678901234567890
ENV JWT_EXPIRATION=86400000

# Run the application
CMD ["java", "-jar", "target/notesapp-0.0.1-SNAPSHOT.jar"]
```

### 2. Create a New Web Service on Render (Docker Method)

1. Log in to your Render account
2. Click "New" and select "Web Service"
3. Connect your repository or paste the repository URL
4. Configure the following settings:

#### Build Settings
- **Name**: notesapp-backend (or any name you prefer)
- **Region**: Choose the region closest to your users
- **Branch**: main (or your preferred branch)
- **Root Directory**: `/backend` (backend directory)
- **Environment**: Docker
- **Dockerfile Path**: `Dockerfile` (this tells Render to use your new Dockerfile in the backend directory)

#### Environment Variables

Add the following environment variables in the Render dashboard:

| Variable Name | Value |
|---------------|-------|
| `SPRING_DATA_MONGODB_URI` | Your MongoDB connection string (pre-configured with your cluster) |
| `JWT_SECRET` | A strong secret key for JWT token generation |
| `JWT_EXPIRATION` | Token expiration time in milliseconds (e.g., 86400000 for 24 hours) |

The MongoDB URI is pre-configured with your cluster:
```
mongodb+srv://shivamyadav2072000_db_user:59wr5wd3V0HHAa0Y@cluster0.jvk8bpk.mongodb.net/notesapp?retryWrites=true&w=majority
```

### 3. Configure Advanced Settings (Optional)

#### Health Check
Render can automatically check your application's health:
- **Health Check Path**: `/health`
- **Protocol**: HTTP
- **Port**: 8080

#### Auto-Deploy
Enable auto-deploy to automatically deploy new commits to your branch.

### 4. Deploy

1. Click "Create Web Service"
2. Render will automatically start building and deploying your application using Docker
3. Wait for the deployment to complete (this may take several minutes as Maven needs to download dependencies)

### 5. Initialize Test Data (Optional)

After deployment, you can initialize test data by making a POST request to your deployed application's `/init` endpoint:

```bash
curl -X POST https://your-app-name.onrender.com/init
```

Replace `your-app-name` with your actual Render app name.

## Environment Variables Reference

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SPRING_DATA_MONGODB_URI` | MongoDB connection string | Yes | mongodb+srv://shivamyadav2072000_db_user:59wr5wd3V0HHAa0Y@cluster0.jvk8bpk.mongodb.net/notesapp?retryWrites=true&w=majority |
| `JWT_SECRET` | Secret key for JWT token signing | Yes | NotesAppSecretKey12345678901234567890123456789012345678901234567890 |
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

To reduce build times on Render, consider these optimizations:

1. **Use Multi-stage Builds** (update your Dockerfile):
   ```dockerfile
   # Build stage
   FROM openjdk:17-jdk-slim AS build
   WORKDIR /app
   COPY pom.xml .
   COPY src ./src
   RUN apt-get update && apt-get install -y maven
   RUN mvn clean package

   # Runtime stage
   FROM openjdk:17-jre-slim
   WORKDIR /app
   COPY --from=build /app/target/notesapp-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE 8080
   CMD ["java", "-jar", "app.jar"]
   ```

2. **Add .dockerignore** to reduce context size:
   Create a `.dockerignore` file in your backend directory:
   ```
   # Build artifacts
   target/
   bin/
   build/

   # IDE files
   *.iml
   *.iws
   *.ipr
   .idea/
   .vscode/
   *.swp
   *.swo

   # OS generated files
   .DS_Store
   .DS_Store?
   ._*
   .Spotlight-V100
   .Trashes
   ehthumbs.db
   Thumbs.db

   # Logs
   *.log

   # Temporary files
   tmp/
   temp/
   ```

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