package com.example.WordGame.modules.roles.user.profile;

import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.Entities.UserProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUser_Username(String username);

    boolean existsByUser(User user);
}
