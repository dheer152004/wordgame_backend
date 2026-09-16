package com.example.WordGame.modules.language.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "languages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "grammar_name", length = 100)
    private String grammarName;

    @Column(name = "grammar_description", columnDefinition = "TEXT")
    private String grammarDescription;

    @Column(name = "grammar_active")
    private Boolean grammarActive = false;

    @Column(name = "display_order")
    private Long displayOrder;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
