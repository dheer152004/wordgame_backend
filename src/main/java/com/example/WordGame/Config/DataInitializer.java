package com.example.WordGame.Config;

import com.example.WordGame.Entities.*;
import com.example.WordGame.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepo categoryRepo;
    private final WordRepo wordRepo;
    private final WordExampleRepo wordExampleRepo;

    @Override
    public void run(String... args) {
        if (categoryRepo.count() == 0) {
            System.out.println("📚 Initializing categories and words...");

            // Create Categories (Don't save them directly in the same line)
            Category slang = new Category();
            slang.setName("Slang");
            categoryRepo.save(slang);

            Category genZ = new Category();
            genZ.setName("Gen Z");
            categoryRepo.save(genZ);

            Category business = new Category();
            business.setName("Business");
            categoryRepo.save(business);

            Category trending = new Category();
            trending.setName("Trending");
            categoryRepo.save(trending);

            // === SLANG CATEGORY WORDS ===
            Word word1 = new Word();
            word1.setWord("no cap");
            word1.setMeaning("For real, not lying, being truthful");
            word1.setCategory(slang);
            word1.setMemeImageUrl("https://example.com/memes/no-cap.jpg");
            wordRepo.save(word1);

            WordExample ex1 = new WordExample();
            ex1.setWord(word1);
            ex1.setExample("No cap, that movie was amazing!");
            wordExampleRepo.save(ex1);

            WordExample ex2 = new WordExample();
            ex2.setWord(word1);
            ex2.setExample("I'm being serious, no cap.");
            wordExampleRepo.save(ex2);

            Word word2 = new Word();
            word2.setWord("rent free");
            word2.setMeaning("Living in your head without paying rent");
            word2.setCategory(slang);
            word2.setMemeImageUrl("https://example.com/memes/rent-free.jpg");
            wordRepo.save(word2);

            WordExample ex3 = new WordExample();
            ex3.setWord(word2);
            ex3.setExample("That song has been living rent free in my head.");
            wordExampleRepo.save(ex3);

            // === GEN Z CATEGORY WORDS ===
            Word word3 = new Word();
            word3.setWord("main character energy");
            word3.setMeaning("Confident, center of attention");
            word3.setCategory(genZ);
            word3.setMemeImageUrl("https://example.com/memes/main-character.jpg");
            wordRepo.save(word3);

            WordExample ex4 = new WordExample();
            ex4.setWord(word3);
            ex4.setExample("She walked in with main character energy!");
            wordExampleRepo.save(ex4);

            Word word4 = new Word();
            word4.setWord("ick");
            word4.setMeaning("Sudden feeling of disgust toward someone");
            word4.setCategory(genZ);
            word4.setMemeImageUrl("https://example.com/memes/ick.jpg");
            wordRepo.save(word4);

            WordExample ex5 = new WordExample();
            ex5.setWord(word4);
            ex5.setExample("He chewed with his mouth open, and I got the ick.");
            wordExampleRepo.save(ex5);

            // === BUSINESS CATEGORY WORDS ===
            Word word5 = new Word();
            word5.setWord("synergy");
            word5.setMeaning("Combined effort producing greater results");
            word5.setCategory(business);
            word5.setMemeImageUrl("https://example.com/memes/synergy.jpg");
            wordRepo.save(word5);

            WordExample ex6 = new WordExample();
            ex6.setWord(word5);
            ex6.setExample("The synergy between teams led to record sales.");
            wordExampleRepo.save(ex6);

            // === TRENDING CATEGORY WORDS ===
            Word word6 = new Word();
            word6.setWord("delulu");
            word6.setMeaning("Delusional, living in fantasy");
            word6.setCategory(trending);
            word6.setMemeImageUrl("https://example.com/memes/delulu.jpg");
            wordRepo.save(word6);

            WordExample ex7 = new WordExample();
            ex7.setWord(word6);
            ex7.setExample("Thinking he'll text you back? That's delulu.");
            wordExampleRepo.save(ex7);

            System.out.println("✅ Added " + categoryRepo.count() + " categories");
            System.out.println("✅ Added " + wordRepo.count() + " words");
            System.out.println("✅ Added " + wordExampleRepo.count() + " examples");
        }
    }
}