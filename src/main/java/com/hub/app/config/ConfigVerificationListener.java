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
            logger.warn("⚠ Konsul tidak tersedia, menggunakan environment variables sebagai fallback");

            // Diagnosa penyebab kegagalan koneksi ke Consul
            diagnoseConsulFailure();

            logger.info("Untuk production, pastikan Consul berjalan dan berisi konfigurasi yang diperlukan");
        } else {
            logger.info("✓ Konfigurasi berhasil dimuat dari Consul");
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

    /**
     * Mendiagnosa penyebab kegagalan koneksi ke Consul
     */
    private void diagnoseConsulFailure() {
        logger.info("=== Diagnosa Kegagalan Koneksi Consul ===");

        // Cek environment variables Consul
        String consulHost = environment.getProperty("spring.cloud.consul.host");
        String consulPort = environment.getProperty("spring.cloud.consul.port");

        logger.info("Consul Host: {}", consulHost != null ? consulHost : "TIDAK DISET");
        logger.info("Consul Port: {}", consulPort != null ? consulPort : "TIDAK DISET");

        // Cek apakah Consul config enabled
        String consulEnabled = environment.getProperty("spring.cloud.consul.config.enabled");
        logger.info("Consul Config Enabled: {}", consulEnabled != null ? consulEnabled : "default (true)");

        // Cek network connectivity (basic check)
        if (consulHost != null && consulPort != null) {
            try {
                // Basic connectivity check - coba ping host
                java.net.InetAddress.getByName(consulHost);
                logger.info("✓ Host '{}' dapat di-resolve", consulHost);
            } catch (Exception e) {
                logger.error("✗ Host '{}' tidak dapat di-resolve: {}", consulHost, e.getMessage());
            }

            // Cek port (basic check)
            try {
                int port = Integer.parseInt(consulPort);
                if (port > 0 && port < 65536) {
                    logger.info("✓ Port {} valid", port);
                } else {
                    logger.error("✗ Port {} tidak valid (harus 1-65535)", port);
                }
            } catch (NumberFormatException e) {
                logger.error("✗ Port '{}' bukan angka valid", consulPort);
            }
        }

        // Cek Spring Cloud Consul dependency
        try {
            Class.forName("org.springframework.cloud.consul.config.ConsulConfigProperties");
            logger.info("✓ Spring Cloud Consul dependency tersedia");
        } catch (ClassNotFoundException e) {
            logger.error("✗ Spring Cloud Consul dependency TIDAK ditemukan di classpath");
        }

        // Saran troubleshooting
        logger.info("=== Saran Troubleshooting ===");
        logger.info("1. Pastikan container Consul berjalan: docker ps | grep consul");
        logger.info("2. Akses Consul UI: http://localhost:8500");
        logger.info("3. Cek konfigurasi di KV store: config/hub-backend/dev/data");
        logger.info("4. Verifikasi network: docker network ls");
        logger.info("5. Cek logs Consul: docker logs consul");

        logger.info("=== Akhir Diagnosa ===");
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