package com.hub.app.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "otp_tokens")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String token;
    private Instant expiresAt;
    private boolean used;
}
