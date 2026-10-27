package com.fhict.campaignmanager.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, unique = true, length = 100)
    private String username;
    @Column(nullable = false, length = 255)
    private String email;
    @Column(nullable = false)
    private String password;
}
