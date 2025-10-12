package com.hub.app.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String address;
    private String ktpNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; 

    private String ktpUrl;
    private String selfieUrl;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status; // UNVERIFIED, VERIFICATION_IN_PROCESS, VERIFIED, REJECTED

    @Enumerated(EnumType.STRING)
    private UserActivityStatus activityStatus; // ACTIVE, INACTIVE

    // Explicit methods for IDEs not running Lombok properly
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getKtpNumber() { return ktpNumber; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getKtpUrl() { return ktpUrl; }
    public String getSelfieUrl() { return selfieUrl; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
}
