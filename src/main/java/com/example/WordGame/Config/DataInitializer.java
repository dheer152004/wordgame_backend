package com.example.WordGame.Config;

import com.example.WordGame.Entities.*;
import com.example.WordGame.Repository.*;
import com.example.WordGame.Repository.Quiz.DailyQuizRepository;
import com.example.WordGame.Repository.Quiz.QuizQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepo categoryRepo;
    private final WordRepo wordRepo;
    private final WordExampleRepo wordExampleRepo;
    private final DailyQuizRepository dailyQuizRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Only initialize if database is empty
        if (categoryRepo.count() == 0) {
            log.info("📚 No data found. Initializing database with sample data...");
            initializeCategories();
            initializeWords();
            initializeDailyQuiz();
            log.info("✅ Database initialization completed!");
        } else {
            log.info("✅ Database already has data. Skipping initialization.");
        }
    }

    private void initializeCategories() {
        log.info("Creating categories...");

        List<Category> categories = Arrays.asList(
                createCategory("Slang", "Modern informal language used in everyday conversation", "https://placehold.co/600x400/FF6B6B/white?text=Slang"),
                createCategory("Gen Z", "Popular terms among Gen Z generation", "https://placehold.co/600x400/4ECDC4/white?text=Gen+Z"),
                createCategory("Business", "Corporate and professional vocabulary", "https://placehold.co/600x400/45B7D1/white?text=Business"),
                createCategory("Trending", "Viral words and phrases right now", "https://placehold.co/600x400/F7B731/white?text=Trending")
        );

        categoryRepo.saveAll(categories);
        log.info("✅ Created {} categories", categories.size());
    }

    private Category createCategory(String name, String description, String imageUrl) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setImageUrl(imageUrl);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    private void initializeWords() {
        log.info("Creating words...");

        Category slang = categoryRepo.findByName("Slang").orElse(null);
        Category genZ = categoryRepo.findByName("Gen Z").orElse(null);
        Category business = categoryRepo.findByName("Business").orElse(null);
        Category trending = categoryRepo.findByName("Trending").orElse(null);

        if (slang == null || genZ == null || business == null || trending == null) {
            log.error("Categories not found! Skipping word initialization.");
            return;
        }

        // Slang Words
        createWordWithExamples("no cap",
                "For real, not lying, being truthful without exaggeration",
                slang,
                "https://placehold.co/600x400/FF6B6B/white?text=No+Cap",
                Arrays.asList("No cap, that movie was incredible!", "I'm being serious, no cap.", "No cap, that's the best pizza I've ever had!")
        );

        createWordWithExamples("rent free",
                "Living in someone's mind without paying, constantly thinking about something",
                slang,
                "https://placehold.co/600x400/FF6B6B/white?text=Rent+Free",
                Arrays.asList("That song has been living rent free in my head all week.", "The drama lives rent free in my mind.", "His comment has been rent free since yesterday.")
        );

        createWordWithExamples("bet",
                "Agreement, okay, for sure; also means 'you can count on it'",
                slang,
                "https://placehold.co/600x400/FF6B6B/white?text=Bet",
                Arrays.asList("You're coming to the party? Bet!", "Bet, I'll be there at 8.", "We're going to win this - bet on it.")
        );

        // Gen Z Words
        createWordWithExamples("main character energy",
                "Confident, center of attention, living life like the protagonist",
                genZ,
                "https://placehold.co/600x400/4ECDC4/white?text=Main+Character",
                Arrays.asList("She walked into the room with such main character energy.", "Wear what makes you feel main character energy.", "Today I'm embracing my main character energy.")
        );

        createWordWithExamples("ick",
                "Sudden feeling of disgust or repulsion toward someone",
                genZ,
                "https://placehold.co/600x400/4ECDC4/white?text=Ick",
                Arrays.asList("He chewed with his mouth open, and I got the ick.", "The way he texts gives me the ick.", "I used to like him, but now I have the ick.")
        );

        createWordWithExamples("delulu",
                "Delusional, living in fantasy, believing something unlikely",
                genZ,
                "https://placehold.co/600x400/4ECDC4/white?text=Delulu",
                Arrays.asList("Thinking he'll text you back? That's delulu.", "I'm delulu enough to think I can finish this today.", "She's delulu if she thinks she can win.")
        );

        // Business Words
        createWordWithExamples("synergy",
                "Combined effort producing greater results than individual efforts",
                business,
                "https://placehold.co/600x400/45B7D1/white?text=Synergy",
                Arrays.asList("The synergy between teams led to record sales.", "We need to create synergy across departments.", "The synergy of our skills made the project successful.")
        );

        createWordWithExamples("leverage",
                "Use something to maximum advantage",
                business,
                "https://placehold.co/600x400/45B7D1/white?text=Leverage",
                Arrays.asList("We need to leverage our existing resources.", "Let's leverage social media for marketing.", "The company leveraged its assets to expand.")
        );

        createWordWithExamples("circle back",
                "Return to a topic later",
                business,
                "https://placehold.co/600x400/45B7D1/white?text=Circle+Back",
                Arrays.asList("Let's circle back to this after the meeting.", "I'll circle back with you on that request.", "We can circle back next week to discuss progress.")
        );

        // Trending Words
        createWordWithExamples("slay",
                "To do something exceptionally well or look amazing",
                trending,
                "https://placehold.co/600x400/F7B731/white?text=Slay",
                Arrays.asList("She slayed that presentation!", "You absolutely slay in that outfit.", "Go out there and slay!")
        );

        createWordWithExamples("period",
                "Emphasis on a statement, end of discussion",
                trending,
                "https://placehold.co/600x400/F7B731/white?text=Period",
                Arrays.asList("I'm the best, period.", "It's the best movie of the year, period.", "No arguments, period.")
        );

        createWordWithExamples("shook",
                "Shocked, surprised, or shaken up",
                trending,
                "https://placehold.co/600x400/F7B731/white?text=Shook",
                Arrays.asList("I'm shook by that plot twist!", "We were all shook when we heard the news.", "The ending left me completely shook.")
        );

        log.info("✅ Created {} words", wordRepo.count());
    }

    private void createWordWithExamples(String wordText, String meaning, Category category,
                                        String memeImageUrl, List<String> examples) {
        // Check if word already exists
        if (wordRepo.findByWord(wordText).isPresent()) {
            log.warn("Word '{}' already exists. Skipping.", wordText);
            return;
        }

        Word word = new Word();
        word.setWord(wordText);
        word.setMeaning(meaning);
        word.setCategory(category);
        word.setMemeImageUrl(memeImageUrl);
        word.setViewCount(0);
        word.setShareCount(0);
        word.setCreatedAt(LocalDateTime.now());

        Word savedWord = wordRepo.save(word);

        // Add examples
        for (String exampleText : examples) {
            WordExample example = new WordExample();
            example.setWord(savedWord);
            example.setExample(exampleText);
            example.setCreatedAt(LocalDateTime.now());
            wordExampleRepo.save(example);
        }
    }

    private void initializeDailyQuiz() {
        log.info("Creating daily quiz for today...");

        LocalDate today = LocalDate.now();

        // Check if quiz already exists for today
        if (dailyQuizRepository.findByQuizDate(today).isPresent()) {
            log.info("Daily quiz already exists for today. Skipping.");
            return;
        }

        // Get random words for quiz
        List<Word> randomWords = wordRepo.findRandomWords(5);

        if (randomWords.size() < 5) {
            log.warn("Not enough words to create daily quiz. Need at least 5 words.");
            return;
        }

        // Create daily quiz
        DailyQuiz dailyQuiz = new DailyQuiz();
        dailyQuiz.setQuizDate(today);
        dailyQuiz.setTitle("Daily Vocabulary Challenge - " + today.toString());
        dailyQuiz.setDescription("Test your knowledge of these trending words!");
        dailyQuiz.setTotalQuestions(5);
        dailyQuiz.setTotalPoints(50);
        dailyQuiz.setIsActive(true);
        dailyQuiz.setCreatedAt(LocalDateTime.now());

        DailyQuiz savedQuiz = dailyQuizRepository.save(dailyQuiz);

        // Create quiz questions
        int orderNumber = 1;
        for (Word word : randomWords) {
            QuizQuestion question = createQuizQuestion(word, savedQuiz, orderNumber++);
            quizQuestionRepository.save(question);
        }

        log.info("✅ Created daily quiz with {} questions", randomWords.size());
    }

    private QuizQuestion createQuizQuestion(Word word, DailyQuiz quiz, int orderNumber) {
        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setWord(word);
        question.setCorrectAnswer(word.getMeaning());
        question.setOrderNumber(orderNumber);
        question.setPoints(10);
        question.setExplanation("'" + word.getWord() + "' means: " + word.getMeaning());

        // Get other words for wrong options
        List<Word> otherWords = wordRepo.findRandomWords(3);

        List<String> options = new java.util.ArrayList<>();
        options.add(word.getMeaning()); // Correct answer

        for (Word wrongWord : otherWords) {
            if (options.size() >= 4) break;
            if (!wrongWord.getMeaning().equalsIgnoreCase(word.getMeaning())) {
                options.add(wrongWord.getMeaning());
            }
        }

        // Fill remaining options
        while (options.size() < 4) {
            options.add("Different meaning");
        }

        // Shuffle options
        java.util.Collections.shuffle(options);

        question.setOptionA(options.get(0));
        question.setOptionB(options.get(1));
        question.setOptionC(options.get(2));
        question.setOptionD(options.get(3));

        return question;
    }
}