#!/bin/bash
# Build script for production environment

echo "Building for production environment..."
./mvnw clean package -Pprod

echo "Build completed. Run with: java -jar target/hub-backend-0.0.1-SNAPSHOT.jar"
echo "Ensure environment variables are set: DB_USERNAME, DB_PASSWORD, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, JWT_SECRET"