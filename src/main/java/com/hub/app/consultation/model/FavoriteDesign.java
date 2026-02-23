package com.hub.app.consultation.model;

import com.hub.app.user.model.HomeOwner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "favorite_designs")
@Data @NoArgsConstructor @AllArgsConstructor
public class FavoriteDesign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_owner_id")
    private HomeOwner homeOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_id")
    private Design design;

    private Boolean isFavorited;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}