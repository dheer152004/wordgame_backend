package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.SavedWordDTO.SaveWordRequestDTO;
import com.example.WordGame.DTO.SavedWordDTO.SavedWordCountDTO;
import com.example.WordGame.DTO.SavedWordDTO.SavedWordResponseDTO;
import com.example.WordGame.Entities.User;
import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.UserSavedWord;
import com.example.WordGame.Repository.UserRepository;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Repository.UserSavedWordRepository;
import com.example.WordGame.Service.SavedWordService;
import com.example.WordGame.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedWordServiceImpl implements SavedWordService {

    private final UserRepository userRepository;
    private final WordRepo wordRepo;
    private final UserSavedWordRepository savedWordRepository;

    @Override
    @Transactional
    @CacheEvict(value = "savedWordsCount", key = "#username")
    public SavedWordResponseDTO saveWord(String username, SaveWordRequestDTO request) {
        log.info("📝 Saving word {} for user {}", request.getWordId(), username);

        User user = getUserByUsername(username);
        Word word = wordRepo.findById(request.getWordId())
                .orElseThrow(() -> new ApiException("Word not found with id: " + request.getWordId()));

        // Check if already saved
        if (savedWordRepository.existsByUserAndWord(user, word)) {
            throw new ApiException("Word already saved by user");
        }

        // Create saved word entry
        UserSavedWord savedWord = new UserSavedWord();
        savedWord.setUser(user);
        savedWord.setWord(word);
        savedWord.setSavedAt(LocalDateTime.now());
        savedWord.setNotes(request.getNotes());

        UserSavedWord saved = savedWordRepository.save(savedWord);

        log.info("✅ Word saved successfully with id: {}", saved.getId());

        return SavedWordResponseDTO.builder()
                .savedWordId(saved.getId())
                .wordId(word.getId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .memeImageUrl(word.getMemeImageUrl())
                .categoryName(word.getCategory().getName())
                .notes(saved.getNotes())
                .savedAt(saved.getSavedAt())
                .build();
    }



    @Override
    @Cacheable(value = "savedWords", key = "#username + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result == null")
    public Page<SavedWordResponseDTO> getSavedWords(String username, Pageable pageable) {
        log.info("📚 Fetching saved words for user {}", username);

        User user = getUserByUsername(username);

        return savedWordRepository.findByUserOrderBySavedAtDesc(user, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"savedWords", "savedWordsCount"}, allEntries = true)
    public void deleteSavedWord(String username, Long wordId) {
        log.info("🗑️ Deleting saved word {} for user {}", wordId, username);

        User user = getUserByUsername(username);
        Word word = wordRepo.findById(wordId)
                .orElseThrow(() -> new ApiException("Word not found with id: " + wordId));

        if (!savedWordRepository.existsByUserAndWord(user, word)) {
            throw new ApiException("Word not found in user's saved list");
        }

        savedWordRepository.deleteByUserAndWord(user, word);
        log.info("✅ Saved word deleted successfully");
    }

    @Override
    public boolean isWordSaved(String username, Long wordId) {
        User user = getUserByUsername(username);
        Word word = wordRepo.findById(wordId).orElse(null);

        if (word == null) {
            return false;
        }

        return savedWordRepository.existsByUserAndWord(user, word);
    }

    @Override
    @Cacheable(value = "savedWordsCount", key = "#username", unless = "#result == null")
    public SavedWordCountDTO getSavedWordsCount(String username) {
        User user = getUserByUsername(username);
        long count = savedWordRepository.countByUser(user);

        return SavedWordCountDTO.builder()
                .totalSavedWords(count)
                .build();
    }

    private SavedWordResponseDTO convertToResponseDTO(UserSavedWord savedWord) {
        Word word = savedWord.getWord();

        return SavedWordResponseDTO.builder()
                .savedWordId(savedWord.getId())
                .wordId(word.getId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .memeImageUrl(word.getMemeImageUrl())
                .categoryName(word.getCategory().getName())
                .notes(savedWord.getNotes())
                .savedAt(savedWord.getSavedAt())
                .build();
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found with username: " + username));
    }
}