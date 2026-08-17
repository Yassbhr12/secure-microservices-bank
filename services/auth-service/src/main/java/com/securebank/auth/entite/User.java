package com.securebank.auth.entite;

import com.securebank.auth.helper.Role;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean enabled;

    private Boolean account_locked;

    private Integer failed_login_attempts;
}
