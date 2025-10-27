package com.hub.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller untuk testing dan memverifikasi konfigurasi Consul
 */
@RestController
@RequestMapping("/api/config")
public class ConfigTestController {

    @Autowired
    private Environment environment;

    /**
     * Endpoint untuk memeriksa status konfigurasi
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkConfiguration() {
        Map<String, Object> config = new HashMap<>();

        // Periksa sumber properti
        List<String> sources = new ArrayList<>();
        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();

        boolean consulFound = false;
        for (PropertySource<?> source : propertySources) {
            sources.add(source.getName());
            if (source.getName().contains("consul")) {
                consulFound = true;
            }
        }
        config.put("propertySources", sources);
        config.put("consulConfigFound", consulFound);
        config.put("configSource", consulFound ? "CONSUL" : "ENVIRONMENT_VARIABLES");

        // Periksa properti penting
        Map<String, Object> properties = new HashMap<>();
        properties.put("databaseUrl", environment.getProperty("spring.datasource.url"));
        properties.put("jwtSecretPresent", environment.getProperty("jwt.secret") != null);
        properties.put("minioEndpoint", environment.getProperty("minio.endpoint"));
        properties.put("minioAccessKeyPresent", environment.getProperty("minio.access-key") != null);
        properties.put("minioSecretKeyPresent", environment.getProperty("minio.secret-key") != null);
        properties.put("simbgBaseUrl", environment.getProperty("simbg.base-url"));
        properties.put("simbgTokenPresent", environment.getProperty("simbg.auth.token") != null);
        properties.put("mailHost", environment.getProperty("spring.mail.host"));
        properties.put("mailUsernamePresent", environment.getProperty("spring.mail.username") != null);
        properties.put("consulHost", environment.getProperty("spring.cloud.consul.host"));
        properties.put("consulPort", environment.getProperty("spring.cloud.consul.port"));

        config.put("properties", properties);

        // Add Consul connectivity diagnosis
        Map<String, Object> consulDiagnosis = new HashMap<>();
        consulDiagnosis.put("host", environment.getProperty("spring.cloud.consul.host"));
        consulDiagnosis.put("port", environment.getProperty("spring.cloud.consul.port"));
        consulDiagnosis.put("configEnabled", environment.getProperty("spring.cloud.consul.config.enabled", "true"));
        consulDiagnosis.put("failFast", environment.getProperty("spring.cloud.consul.config.fail-fast", "false"));

        // Basic connectivity check
        String consulHost = environment.getProperty("spring.cloud.consul.host");
        String consulPort = environment.getProperty("spring.cloud.consul.port");
        boolean hostReachable = false;
        if (consulHost != null && consulPort != null) {
            try {
                java.net.InetAddress.getByName(consulHost);
                hostReachable = true;
            } catch (Exception e) {
                hostReachable = false;
            }
        }
        consulDiagnosis.put("hostReachable", hostReachable);

        config.put("consulDiagnosis", consulDiagnosis);

        // Status keseluruhan
        config.put("status", consulFound ? "CONFIG_LOADED_FROM_CONSUL" : "USING_DEFAULT_CONFIG");

        return ResponseEntity.ok(config);
    }

    /**
     * Endpoint untuk mendapatkan semua properti (untuk debugging)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, String>> getAllProperties() {
        Map<String, String> allProps = new HashMap<>();

        // Properti yang penting untuk dicek
        String[] importantProps = {
            "spring.datasource.url",
            "spring.datasource.username",
            "jwt.secret",
            "minio.endpoint",
            "minio.access-key",
            "minio.secret-key",
            "simbg.base-url",
            "simbg.auth.token",
            "spring.mail.host",
            "spring.mail.username",
            "spring.cloud.consul.host",
            "spring.cloud.consul.port",
            "spring.application.name"
        };

        for (String prop : importantProps) {
            String value = environment.getProperty(prop);
            if (value != null) {
                // Sembunyikan nilai sensitif
                if (prop.contains("secret") || prop.contains("password") || prop.contains("token")) {
                    allProps.put(prop, "[HIDDEN:" + value.length() + " chars]");
                } else {
                    allProps.put(prop, value);
                }
            } else {
                allProps.put(prop, "NOT_SET");
            }
        }

        return ResponseEntity.ok(allProps);
    }
}