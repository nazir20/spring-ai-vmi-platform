package com.orbit.auth_users.entity;

import com.orbit.role.entity.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required.")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Email is required.")
    @Email
    @Size(max = 255)
    @Column(unique = true)
    private String email;

    @Size(max = 20)
    private String phoneNumber;

    @NotBlank(message = "Password is required.")
    @Size(min = 8)
    private String password;

    @Builder.Default
    private boolean isActive = false;

    @Builder.Default
    private boolean mustChangePassword = true;

    private Instant passwordChangedAt;

    @Builder.Default
    private int tokenVersion = 0;

    private String invitationTokenHash;
    private Instant invitationAcceptedAt;
    private Instant invitationExpiresAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
