package com.example.WordGame.Repository;

import com.example.WordGame.Entities.User;
import com.example.WordGame.Entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);
    boolean existsByUser(User user);
}
