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
    var goal: ETrainingPlanInputGoal? = null, // finish, time (e.g., "4:00")

    @Column(name = "max_distance", nullable = false)
    var maxDistance: Double? = null, // km

    @Column(name = "average_pace", nullable = false)
    var averagePace: Double? = null, // minutes/km

    @Column(name = "days_per_week", nullable = false)
    var daysPerWeek: Int? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

//    @Embedded
//var config: MarathonTrainingConfig = MarathonTrainingConfig()
)

enum class ETrainingPlanInputLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}

enum class ETrainingPlanInputGoal {
    MARATHON_FINISH, MARATHON_TIME, NO_INJURY, OTHER
}