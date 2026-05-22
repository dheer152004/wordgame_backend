package com.example.WordGame.Repository;

import com.example.WordGame.Entities.User;
import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.UserSavedWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSavedWordRepository extends JpaRepository<UserSavedWord, Long> {

    // Get all saved words for a user with pagination
    Page<UserSavedWord> findByUserOrderBySavedAtDesc(User user, Pageable pageable);

    // Check if user already saved a specific word
    boolean existsByUserAndWord(User user, Word word);

    // Find saved word by user and word (for delete operation)
    Optional<UserSavedWord> findByUserAndWord(User user, Word word);

    // Count saved words for a user
    long countByUser(User user);

    // Delete a saved word by user and word
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSavedWord usw WHERE usw.user = :user AND usw.word = :word")
    void deleteByUserAndWord(@Param("user") User user, @Param("word") Word word);

    // Get saved word IDs for a user (for quick checking)
    @Query("SELECT usw.word.id FROM UserSavedWord usw WHERE usw.user = :user")
    List<Long> findSavedWordIdsByUser(@Param("user") User user);
}
