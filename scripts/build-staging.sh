#!/bin/bash
# Build script for staging environment

echo "Building for staging environment..."
./mvnw clean package -Pstaging

echo "Build completed. Run with: java -jar target/hub-backend-0.0.1-SNAPSHOT.jar"
echo "Ensure environment variables are set: DB_USERNAME, DB_PASSWORD, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, JWT_SECRET"