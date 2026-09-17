package com.example.WordGame.modules.GrammarValues.entity;

import com.example.WordGame.modules.GrammarCategories.entity.GrammarCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grammar_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrammarValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grammar_category_id", nullable = false)
    private GrammarCategory grammarCategory;

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
