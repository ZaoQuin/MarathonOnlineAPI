package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*

@Entity
@Table(name = "training_feedback")
data class TrainingFeedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_day_id", nullable = false)
    var trainingDay: TrainingDay? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var difficultyRating: EDifficultyRating,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var feelingRating: EFeelingRating,

    @Column(nullable = true, length = 500)
    var notes: String? = null,
)

enum class EDifficultyRating {
    VERY_EASY,
    EASY,
    MODERATE,
    HARD,
    VERY_HARD
}

enum class EFeelingRating {
    EXCELLENT,
    GOOD,
    OKAY,
    TIRED,
    EXHAUSTED
}