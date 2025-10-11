#!/bin/bash
# Build script for development environment

echo "Building for development environment..."
./mvnw clean package -Pdev

echo "Build completed. Run with: java -jar target/hub-backend-0.0.1-SNAPSHOT.jar"