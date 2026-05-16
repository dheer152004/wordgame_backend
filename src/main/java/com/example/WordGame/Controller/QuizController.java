package com.example.WordGame.Controller;

import com.example.WordGame.DTO.QuizDTO.QuizHistoryDTO;
import com.example.WordGame.DTO.QuizDTO.QuizQuestionResponseDTO;
import com.example.WordGame.DTO.QuizDTO.QuizResultResponseDTO;
import com.example.WordGame.DTO.QuizDTO.QuizSubmissionRequestDTO;
import com.example.WordGame.Service.QuizService;
import com.example.WordGame.exceptions.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;

    /**
     * GET /api/quiz/today
     * Get today's quiz questions (5 random words with options)
     */
    @GetMapping("/today")
    public ResponseEntity<List<QuizQuestionResponseDTO>> getTodayQuiz(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        List<QuizQuestionResponseDTO> questions = quizService.getTodayQuiz(username);
        return ResponseEntity.ok(questions);
    }

    /**
     * POST /api/quiz/submit
     * Submit quiz answers and get results with XP rewards
     */
    @PostMapping("/submit")
    public ResponseEntity<QuizResultResponseDTO> submitQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody QuizSubmissionRequestDTO submission) {
        String username = userDetails.getUsername();
        QuizResultResponseDTO result = quizService.submitQuiz(username, submission);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/quiz/history
     * Get user's quiz history with pagination
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getQuizHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedAt"));
        Page<QuizHistoryDTO> history = quizService.getQuizHistory(username, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", history.getContent());
        response.put("currentPage", history.getNumber());
        response.put("totalItems", history.getTotalElements());
        response.put("totalPages", history.getTotalPages());
        response.put("hasNext", history.hasNext());
        response.put("hasPrevious", history.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/quiz/status
     * Check if user has completed today's quiz
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getQuizStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        boolean completed = quizService.hasCompletedTodayQuiz(username);

        Map<String, Object> response = new HashMap<>();
        response.put("completedToday", completed);
        response.put("message", completed ? "You have already completed today's quiz!" : "You can take today's quiz");

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/quiz/stats
     * Get user's quiz statistics (total quizzes, average score, XP, level, streak)
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getQuizStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Object stats = quizService.getQuizStats(username);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/quiz/today/available
     * Check if today's quiz is available and not completed
     */
    @GetMapping("/today/available")
    public ResponseEntity<Map<String, Object>> isQuizAvailable(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        boolean completed = quizService.hasCompletedTodayQuiz(username);

        Map<String, Object> response = new HashMap<>();
        response.put("available", !completed);
        response.put("completed", completed);

        if (!completed) {
            response.put("message", "Quiz is available! Take it now to earn XP.");
        } else {
            response.put("message", "You've already earned your XP today! Come back tomorrow.");
        }

        return ResponseEntity.ok(response);
    }

}
