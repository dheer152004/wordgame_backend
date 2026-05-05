package com.example.WordGame.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    private String platform;

    @Column(name = "share_type")
    private String shareType;

    @Column(name = "is_successful")
    private Boolean isSuccessful = true;

    @Column(name = "shared_at")
    private LocalDateTime sharedAt = LocalDateTime.now();
}
