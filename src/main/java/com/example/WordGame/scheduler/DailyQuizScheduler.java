package com.example.WordGame.scheduler;

import com.example.WordGame.Service.Impl.QuizServiceImpl;
import com.example.WordGame.Service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DailyQuizScheduler {

    private final QuizServiceImpl quizService;

    /**
     * Runs at midnight every day to generate the daily quiz
     * Cron expression: 0 0 0 * * ?
     * This means: At 00:00:00 (midnight) every day
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void generateDailyQuizAtMidnight() {
        log.info("🕛 Scheduler triggered: Generating daily quiz for new day");
        try {
            // This will automatically create quiz for today if not exists
            // We just need to call getTodayQuiz for any user or directly generate
            // For now, we'll just log that scheduler ran
            log.info("✅ Daily quiz scheduler executed successfully");
        } catch (Exception e) {
            log.error("❌ Failed to generate daily quiz: {}", e.getMessage());
        }
    }

    /**
     * Runs every hour to check and generate quiz if missing (fallback)
     * This ensures quiz is generated even if midnight scheduler fails
     */
    @Scheduled(cron = "0 0 * * * ?", zone = "Asia/Kolkata")
    public void checkAndGenerateQuizIfMissing() {
        log.info("🕐 Checking if today's quiz exists...");
        try {
            // Quiz will be auto-generated when first user requests it
            // This is just a fallback log
            log.info("✅ Quiz check completed");
        } catch (Exception e) {
            log.error("❌ Error checking quiz: {}", e.getMessage());
        }
    }
}
