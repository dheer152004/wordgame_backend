package com.example.WordGame.modules.streak.service;

import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.repository.UserRepository;
import com.example.WordGame.modules.streak.repository.StreakActivityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(StreakService.class)
class StreakConcurrencyTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreakService streakService;

    @Autowired
    private StreakActivityRepository streakActivityRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentSameDayCompletionsIncrementOnlyOnce() throws Exception {
        User user = new User();
        user.setUsername("streak-user");
        user.setEmail("streak@example.com");
        user.setPassword("test-password");
        user.setCurrentStreak(2);
        user.setLongestStreak(2);
        user.setLastQuizDate(LocalDate.of(2026, 10, 1));
        userRepository.saveAndFlush(user);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> recordCompletion(ready, start));
            Future<?> second = executor.submit(() -> recordCompletion(ready, start));
            ready.await();
            start.countDown();
            first.get();
            second.get();
        } finally {
            executor.shutdownNow();
        }

        User updated = userRepository.findByUsername("streak-user").orElseThrow();
        assertEquals(3, updated.getCurrentStreak());
        assertEquals(3, updated.getLongestStreak());
        assertEquals(1, streakActivityRepository.countByUser(updated));
    }

    private void recordCompletion(CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            start.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            User lockedUser = userRepository.findByUsernameForUpdate("streak-user").orElseThrow();
            streakService.recordQuizCompletion(lockedUser, LocalDate.of(2026, 10, 2));
            userRepository.saveAndFlush(lockedUser);
        });
    }
}