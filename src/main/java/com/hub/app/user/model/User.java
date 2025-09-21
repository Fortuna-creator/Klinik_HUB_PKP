package com.hub.app.user.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; 

    private String ktpUrl;
    private String selfieUrl;

    @Enumerated(EnumType.STRING)
    private UserStatus status; // UNVERIFIED, PENDING_APPROVAL, ACTIVE, REJECTED
}
