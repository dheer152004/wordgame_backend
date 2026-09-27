package com.example.WordGame.modules.WOTD.repository;

import com.example.WordGame.modules.WOTD.entity.WordOfTheDay;
import com.example.WordGame.modules.WOTD.enums.WordOfTheDayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordOfTheDayRepository extends JpaRepository<WordOfTheDay, Long> {
    Optional<WordOfTheDay> findByPublishOn(LocalDate publishOn);

    Optional<WordOfTheDay> findByPublishOnAndStatus(LocalDate publishOn, WordOfTheDayStatus status);

    List<WordOfTheDay> findByPublishOnBetweenOrderByPublishOnAsc(LocalDate from, LocalDate to);
}