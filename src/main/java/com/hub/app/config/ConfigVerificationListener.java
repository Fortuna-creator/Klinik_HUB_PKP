package com.hub.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Listener untuk memverifikasi bahwa konfigurasi telah berhasil dimuat dari Consul
 */
@Component
public class ConfigVerificationListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigVerificationListener.class);

    @Autowired
    private Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("=== Memverifikasi Sumber Konfigurasi ===");

        boolean consulConfigFound = false;
        MutablePropertySources sources = ((AbstractEnvironment) environment).getPropertySources();

        for (PropertySource<?> source : sources) {
            if (source.getName().contains("consul")) {
                consulConfigFound = true;
                logger.info("✓ Sumber konfigurasi Consul ditemukan: {}", source.getName());
                break;
            }
        }

        if (!consulConfigFound) {
            logger.error("✗ TIDAK ADA sumber konfigurasi Consul yang ditemukan!");
            logger.error("Aplikasi mungkin menggunakan konfigurasi default atau environment variables");
        }

        // Verifikasi properti penting
        logger.info("=== Verifikasi Properti Konfigurasi ===");
        verifyProperty("spring.datasource.url", "Database URL");
        verifyProperty("spring.datasource.username", "Database Username");
        verifyProperty("jwt.secret", "JWT Secret", true);
        verifyProperty("minio.endpoint", "MinIO Endpoint");
        verifyProperty("minio.access-key", "MinIO Access Key", true);
        verifyProperty("minio.secret-key", "MinIO Secret Key", true);
        verifyProperty("simbg.base-url", "SIMBG Base URL");
        verifyProperty("simbg.auth.token", "SIMBG Token", true);
        verifyProperty("spring.mail.host", "Mail Host");
        verifyProperty("spring.mail.username", "Mail Username", true);
        verifyProperty("spring.mail.password", "Mail Password", true);

        logger.info("=== Verifikasi Konfigurasi Selesai ===");
    }

    private void verifyProperty(String propertyName, String displayName) {
        verifyProperty(propertyName, displayName, false);
    }

    private void verifyProperty(String propertyName, String displayName, boolean isSensitive) {
        String value = environment.getProperty(propertyName);
        if (value != null && !value.isEmpty()) {
            if (isSensitive) {
                logger.info("✓ {}: [TERSET] (panjang: {} karakter)", displayName, value.length());
            } else {
                logger.info("✓ {}: {}", displayName, value);
            }
        } else {
            logger.warn("⚠ {}: TIDAK DISET", displayName);
        }
    }
}