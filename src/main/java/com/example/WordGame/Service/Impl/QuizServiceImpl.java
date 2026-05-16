package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.QuizDTO.QuizHistoryDTO;
import com.example.WordGame.DTO.QuizDTO.QuizQuestionResponseDTO;
import com.example.WordGame.DTO.QuizDTO.QuizResultResponseDTO;
import com.example.WordGame.DTO.QuizDTO.QuizSubmissionRequestDTO;
import com.example.WordGame.Entities.*;
import com.example.WordGame.Repository.Quiz.DailyQuizRepository;
import com.example.WordGame.Repository.Quiz.QuizAttemptRepository;
import com.example.WordGame.Repository.Quiz.QuizQuestionRepository;
import com.example.WordGame.Repository.Quiz.UserAnswerRepository;
import com.example.WordGame.Repository.UserRepository;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Service.QuizService;
import com.example.WordGame.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final UserRepository userRepository;
    private final DailyQuizRepository dailyQuizRepository;
    private final WordRepo wordRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;

    private static final int XP_PER_QUESTION = 10;
    private static final int BONUS_XP_FOR_PERFECT = 50;
    private static final int BONUS_XP_FOR_STREAK = 20;
    private static final int QUIZ_SIZE = 5;

    @Override
    public List<QuizQuestionResponseDTO> getTodayQuiz(String userEmail) {
        User user = getUserByEmail(userEmail);

        // Check if already completed today's quiz
        if (quizAttemptRepository.hasCompletedTodayQuiz(user)) {
            throw new ApiException("You have already completed today's quiz!");
        }

        // Get or create today's quiz
        DailyQuiz todayQuiz = getOrCreateTodayQuiz();

        // Get all questions for today's quiz
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizOrderByOrderNumberAsc(todayQuiz);

        // 🛠️ THE FIX: If the quiz exists but has no questions, generate them now!
        if (questions.isEmpty()) {
            generateQuizQuestionsOptimized(todayQuiz); // Generate them
            questions = quizQuestionRepository.findByQuizOrderByOrderNumberAsc(todayQuiz); // Fetch again

            // If it's STILL empty after generating, then we have a real problem (e.g., not enough words in DB)
            if (questions.isEmpty()) {
                throw new ApiException("No questions available for today's quiz, and generation failed. Check your Word database.");
            }
        }

        // Convert to DTO
        return questions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuizResultResponseDTO submitQuiz(String userEmail, QuizSubmissionRequestDTO submission) {
        User user = getUserByEmail(userEmail);

        // Check if already completed today
        if (quizAttemptRepository.hasCompletedTodayQuiz(user)) {
            throw new ApiException("You have already completed today's quiz!");
        }

        // Get today's quiz
        DailyQuiz todayQuiz = dailyQuizRepository.findTodayQuiz()
                .orElseThrow(() -> new ApiException("No quiz available for today!"));

        // Get all questions
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizOrderByOrderNumberAsc(todayQuiz);

        if (questions.size() != submission.getAnswers().size()) {
            throw new ApiException("Invalid number of answers submitted!");
        }

        // 1. Create quiz attempt instance (Do not save to DB yet)
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(todayQuiz);
        attempt.setTotalPossible(todayQuiz.getTotalPoints());
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(5));
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setIsCompleted(true);
        attempt.setTimeTakenSeconds(calculateTotalTime(submission));

        // Ensure the list is initialized for adding child elements
        attempt.setAnswers(new ArrayList<>());

        // Calculate score and build answer relationships
        int totalScore = 0;
        int totalXpEarned = 0;
        List<QuizResultResponseDTO.QuestionResultDTO> results = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            QuizSubmissionRequestDTO.AnswerDTO answer = submission.getAnswers().get(i);

            boolean isCorrect = answer.getSelectedOption().equalsIgnoreCase(
                    getCorrectOptionLetter(question)
            );

            int pointsEarned = isCorrect ? question.getPoints() : 0;
            totalScore += pointsEarned;
            totalXpEarned += isCorrect ? XP_PER_QUESTION : 0;

            // 2. Instantiate the child answer entity
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setAttempt(attempt); // Child points to Parent
            userAnswer.setQuestion(question);
            userAnswer.setSelectedOption(answer.getSelectedOption());
            userAnswer.setIsCorrect(isCorrect);
            userAnswer.setPointsEarned(pointsEarned);
            userAnswer.setTimeTakenMs(answer.getTimeTakenMs());
            userAnswer.setAnsweredAt(LocalDateTime.now());

            // Crucial Cascade Step: Parent points to Child in memory
            attempt.getAnswers().add(userAnswer);

            // Add to results collection for response DTO
            results.add(QuizResultResponseDTO.QuestionResultDTO.builder()
                    .questionId(question.getId())
                    .word(question.getWord().getWord())
                    .isCorrect(isCorrect)
                    .correctAnswer(question.getCorrectAnswer())
                    .yourAnswer(getOptionText(question, answer.getSelectedOption()))
                    .explanation(question.getExplanation())
                    .pointsEarned(pointsEarned)
                    .build());
        }

        // Calculate percentage score
        BigDecimal percentage = BigDecimal.valueOf(totalScore)
                .divide(BigDecimal.valueOf(todayQuiz.getTotalPoints()), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Add bonus XP for perfect score
        if (totalScore == todayQuiz.getTotalPoints()) {
            totalXpEarned += BONUS_XP_FOR_PERFECT;
        }

        // Finalize attempt details
        attempt.setScore(totalScore);
        attempt.setPercentage(percentage);
        attempt.setXpEarned(totalXpEarned);

        // 3. Save the Parent ONE TIME
        // Because of cascade = CascadeType.ALL, this saves the attempt AND all child answers automatically
        quizAttemptRepository.save(attempt);

        // Update user statistics
        int newTotalXp = user.getTotalXp() + totalXpEarned;
        user.setTotalXp(newTotalXp);

        // Update streak
        int updatedStreak = updateStreak(user);
        user.setCurrentStreak(updatedStreak);
        if (updatedStreak > user.getLongestStreak()) {
            user.setLongestStreak(updatedStreak);
        }

        // Update level
        int newLevel = calculateLevel(newTotalXp);
        user.setLevel(newLevel);

        // Add weekly streak bonus XP (if streak >= 7 days)
        if (updatedStreak >= 7) {
            totalXpEarned += BONUS_XP_FOR_STREAK;
            user.setTotalXp(user.getTotalXp() + BONUS_XP_FOR_STREAK);
        }

        user.setLastActive(LocalDateTime.now());
        user.setLastQuizDate(LocalDate.now());
        userRepository.save(user);

        // Build and return final response payload
        return QuizResultResponseDTO.builder()
                .score(totalScore)
                .totalPossible(todayQuiz.getTotalPoints())
                .percentage(percentage)
                .xpEarned(totalXpEarned)
                .newTotalXp(user.getTotalXp())
                .newLevel(user.getLevel())
                .currentStreak(user.getCurrentStreak())
                .message(getResultMessage(percentage))
                .details(results)
                .build();
    }

    @Override
    public Page<QuizHistoryDTO> getQuizHistory(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return quizAttemptRepository.findByUserOrderByCompletedAtDesc(user, pageable)
                .map(attempt -> QuizHistoryDTO.builder()
                        .attemptId(attempt.getId())
                        .quizDate(attempt.getQuiz().getQuizDate())
                        .quizTitle(attempt.getQuiz().getTitle())
                        .score(attempt.getScore())
                        .totalPossible(attempt.getTotalPossible())
                        .percentage(attempt.getPercentage())
                        .xpEarned(attempt.getXpEarned())
                        .completedAt(attempt.getCompletedAt())
                        .build());
    }

    @Override
    public boolean hasCompletedTodayQuiz(String userEmail) {
        User user = getUserByEmail(userEmail);
        return quizAttemptRepository.hasCompletedTodayQuiz(user);
    }

    @Override
    public Object getQuizStats(String userEmail) {
        User user = getUserByEmail(userEmail);

        Long totalQuizzes = quizAttemptRepository.countByUser(user);
        Double avgScore = quizAttemptRepository.getAverageScoreByUser(user);
        Integer totalXp = quizAttemptRepository.getTotalXpByUser(user);

        int xpInCurrentLevel = user.getTotalXp() % 100;
        int xpToNextLevel = 100 - xpInCurrentLevel;
        int levelProgress = (xpInCurrentLevel * 100) / 100;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuizzes", totalQuizzes != null ? totalQuizzes : 0);
        stats.put("averageScore", avgScore != null ? Math.round(avgScore) : 0);
        stats.put("totalXpEarned", totalXp != null ? totalXp : 0);
        stats.put("currentStreak", user.getCurrentStreak());
        stats.put("longestStreak", user.getLongestStreak());
        stats.put("level", user.getLevel());
        stats.put("totalXp", user.getTotalXp());
        stats.put("xpToNextLevel", xpToNextLevel);
        stats.put("levelProgress", levelProgress);

        return stats;
    }

    // ============ OPTIMIZED HELPER METHODS ============

    @Transactional
    DailyQuiz getOrCreateTodayQuiz() {
        LocalDate today = LocalDate.now();

        return dailyQuizRepository.findByQuizDate(today)
                .orElseGet(() -> {
                    // Create new daily quiz
                    DailyQuiz newQuiz = new DailyQuiz();
                    newQuiz.setQuizDate(today);
                    newQuiz.setTitle("Daily Quiz - " + today);
                    newQuiz.setDescription("Test your slang vocabulary!");
                    newQuiz.setTotalQuestions(QUIZ_SIZE);
                    newQuiz.setTotalPoints(QUIZ_SIZE * 10);
                    newQuiz.setIsActive(true);
                    newQuiz.setCreatedAt(LocalDateTime.now());

                    DailyQuiz savedQuiz = dailyQuizRepository.save(newQuiz);

                    // Generate questions efficiently
                    generateQuizQuestionsOptimized(savedQuiz);

                    return savedQuiz;
                });
    }

    /**
     * OPTIMIZED: Only fetches exactly what is needed from database
     * No findAll() - uses native RANDOM() LIMIT query
     */
    void generateQuizQuestionsOptimized(DailyQuiz quiz) {
        // Quick validation - only gets count, not all records
        long wordCount = wordRepository.getTotalWordCount();

        if (wordCount < QUIZ_SIZE) {
            throw new ApiException("Need at least " + QUIZ_SIZE + " words. Found: " + wordCount);
        }

        // Fetch exactly 5 random words - ONE efficient query
        List<Word> randomWords = wordRepository.findRandomWords(QUIZ_SIZE);

        // Fetch additional random words for wrong options (cached in same session)
        List<Word> wrongWordsPool = wordRepository.findRandomWords(QUIZ_SIZE * 2);

        int orderNumber = 1;
        for (Word word : randomWords) {
            QuizQuestion question = createQuestionOptimized(word, quiz, orderNumber++, wrongWordsPool);
            quizQuestionRepository.save(question);
        }
    }

    QuizQuestion createQuestionOptimized(Word word, DailyQuiz quiz, int orderNumber, List<Word> wrongWordsPool) {
        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setWord(word);
        question.setCorrectAnswer(word.getMeaning());
        question.setOrderNumber(orderNumber);
        question.setPoints(10);
        question.setExplanation("\"" + word.getWord() + "\" means: " + word.getMeaning());

        // Build options efficiently
        List<String> options = new ArrayList<>();
        options.add(word.getMeaning()); // Correct answer

        // Use pre-fetched pool for wrong options
        for (Word wrongWord : wrongWordsPool) {
            if (options.size() >= 4) break;
            // FIXED: Compare primitive long values directly
            if (wrongWord.getId() != word.getId() &&
                    !wrongWord.getMeaning().equalsIgnoreCase(word.getMeaning())) {
                options.add(wrongWord.getMeaning());
            }
        }

        // Fill remaining slots if needed
        while (options.size() < 4) {
            options.add("Different meaning");
        }

        Collections.shuffle(options);

        question.setOptionA(options.get(0));
        question.setOptionB(options.get(1));
        question.setOptionC(options.get(2));
        question.setOptionD(options.get(3));

        return question;
    }

    QuizQuestionResponseDTO convertToResponseDTO(QuizQuestion question) {
        List<String> options = Arrays.asList(
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD()
        );

        return QuizQuestionResponseDTO.builder()
                .questionId(question.getId())
                .wordId(question.getWord().getId())
                .word(question.getWord().getWord())
                .options(options)
                .points(question.getPoints())
                .build();
    }

    String getCorrectOptionLetter(QuizQuestion question) {
        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer.equals(question.getOptionA())) return "A";
        if (correctAnswer.equals(question.getOptionB())) return "B";
        if (correctAnswer.equals(question.getOptionC())) return "C";
        if (correctAnswer.equals(question.getOptionD())) return "D";
        return "A";
    }

    String getOptionText(QuizQuestion question, String selectedLetter) {
        if (selectedLetter == null) return "No answer";
        switch (selectedLetter.toUpperCase()) {
            case "A": return question.getOptionA();
            case "B": return question.getOptionB();
            case "C": return question.getOptionC();
            case "D": return question.getOptionD();
            default: return "Invalid option";
        }
    }

    int calculateTotalTime(QuizSubmissionRequestDTO submission) {
        return submission.getAnswers().stream()
                .mapToInt(QuizSubmissionRequestDTO.AnswerDTO::getTimeTakenMs)
                .sum() / 1000;
    }

    int updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastQuizDate = user.getLastQuizDate();

        if (lastQuizDate == null) return 1;
        if (lastQuizDate.equals(today.minusDays(1))) return user.getCurrentStreak() + 1;
        if (lastQuizDate.equals(today)) return user.getCurrentStreak();
        return 1;
    }

    int calculateLevel(int totalXp) {
        return (totalXp / 100) + 1;
    }

    String getResultMessage(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.valueOf(100)) == 0) {
            return "🎉 Perfect! You're a word master! +50 XP Bonus!";
        } else if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "🌟 Excellent! Great job learning!";
        } else if (percentage.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "📚 Good work! Keep practicing!";
        } else if (percentage.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "💪 Nice try! Review the words and try again!";
        } else {
            return "📖 Keep learning! You'll get better each day!";
        }
    }

    User getUserByEmail(String email) {
        return userRepository.findByUsername(email)
                .orElseThrow(() -> new ApiException("User not found with email: " + email));
    }
}