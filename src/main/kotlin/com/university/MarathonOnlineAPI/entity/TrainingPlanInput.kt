package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "training_plan_input")
data class TrainingPlanInput(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_id", nullable = false)
    var user: User? = null,

    @Column(nullable = false)
    var level: ETrainingPlanInputLevel? = null,

    @Column(nullable = false)
    var goal: ETrainingPlanInputGoal? = null,

    @Column(name = "max_distance", nullable = false)
    var maxDistance: Double? = null,

    @Column(name = "average_pace", nullable = false)
    var averagePace: Double? = null,

    @Column(name = "training_weeks", nullable = false)
    var trainingWeeks: Int? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class ETrainingPlanInputLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}

enum class ETrainingPlanInputGoal {
    MARATHON_FINISH, MARATHON_TIME, HALF_MARATHON_FINISH, HALF_MARATHON_TIME, TEN_KM_FINISH, TEN_KM_TIME, FIVE_KM_FINISH, FIVE_KM_TIME, OTHER
}