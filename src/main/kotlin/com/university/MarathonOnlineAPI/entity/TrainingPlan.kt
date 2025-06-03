package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "training_plan")
data class TrainingPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_id", nullable = false)
    var user: User,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    var input: TrainingPlanInput,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var startDate: LocalDateTime? = null,

    @Column(nullable = true)
    var endDate: LocalDateTime? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ETrainingPlanStatus = ETrainingPlanStatus.ACTIVE,

    @OneToMany(mappedBy = "plan", cascade = [CascadeType.ALL])
    var trainingDays: List<TrainingDay> = mutableListOf()
)

enum class ETrainingPlanStatus {
    ACTIVE,
    COMPLETED,
    ARCHIVED
}