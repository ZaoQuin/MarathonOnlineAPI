package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "training_day")
data class TrainingDay(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: TrainingPlan? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: TrainingSession? = null,

    @Column
    var week: Int? = null,

    @Column(name = "day_of_week")
    var dayOfWeek: Int? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", referencedColumnName = "id")
    var record: Record? = null,

    @Column
    var status: ETrainingDayStatus?= null,

    @Column
    var dateTime: LocalDateTime? = null,

    @OneToOne(mappedBy = "trainingDay", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var trainingFeedback: TrainingFeedback? = null,

    @Column
    var completionPercentage: Double? = null
)

enum class ETrainingDayStatus {
    ACTIVE,
    COMPLETED,
    PARTIALLY_COMPLETED,
    SKIPPED,
    MISSED
}