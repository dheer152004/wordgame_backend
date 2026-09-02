package com.example.WordGame.modules.roles.repository;

import com.example.WordGame.modules.roles.LeaderboardCache;
import com.example.WordGame.modules.roles.user.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardCacheRepository extends JpaRepository<LeaderboardCache, Long> {
    void deleteByUser(User user);
}
