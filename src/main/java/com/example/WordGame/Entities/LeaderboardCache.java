package com.example.WordGame.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String username;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "total_xp")
    private Integer totalXp = 0;

    @Column(name = "weekly_xp")
    private Integer weeklyXp = 0;

    @Column(name = "monthly_xp")
    private Integer monthlyXp = 0;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "rank_weekly")
    private Integer rankWeekly;

    @Column(name = "rank_monthly")
    private Integer rankMonthly;

    @Column(name = "rank_all_time")
    private Integer rankAllTime;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}