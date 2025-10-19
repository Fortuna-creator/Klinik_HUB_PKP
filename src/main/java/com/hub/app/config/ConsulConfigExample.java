package com.hub.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsulConfigExample {

    @Value("${database.url}")
    private String databaseUrl;

    @Value("${database.username}")
    private String databaseUsername;

    @Value("${database.password}")
    private String databasePassword;

    // Getters for accessing the properties
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    // Example method to demonstrate usage
    public void printDatabaseConfig() {
        System.out.println("Database URL: " + databaseUrl);
        System.out.println("Database Username: " + databaseUsername);
        System.out.println("Database Password: " + databasePassword);
    }
}