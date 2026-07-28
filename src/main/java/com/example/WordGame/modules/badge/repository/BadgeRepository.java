package com.example.WordGame.modules.badge.repository;

import com.example.WordGame.modules.badge.Entities.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
