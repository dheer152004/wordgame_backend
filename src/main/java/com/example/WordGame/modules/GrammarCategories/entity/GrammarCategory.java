package com.example.WordGame.modules.GrammarCategories.entity;

import com.example.WordGame.modules.language.entity.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grammar_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrammarCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Long displayOrder;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
