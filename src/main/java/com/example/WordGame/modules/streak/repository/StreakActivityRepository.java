package com.example.WordGame.modules.streak.repository;

import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.streak.entity.StreakActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StreakActivityRepository extends JpaRepository<StreakActivity, Long> {
    boolean existsByUserAndActivityDate(User user, LocalDate activityDate);

    long countByUser(User user);

    Optional<StreakActivity> findFirstByUserOrderByActivityDateAsc(User user);

    List<StreakActivity> findByUserAndActivityDateBetweenOrderByActivityDateAsc(
            User user, LocalDate startDate, LocalDate endDate);
}