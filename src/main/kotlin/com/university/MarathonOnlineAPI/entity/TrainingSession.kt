package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*

@Entity
@Table(name = "training_session")
data class TrainingSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: ETrainingSessionType,

    @Column(nullable = false)
    var distance: Double? = null,

    @Column(nullable = false)
    var pace: Double? = null,

    @Column(nullable = true)
    var notes: String? = null,

    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var trainingDays: MutableList<TrainingDay> = mutableListOf()
)

enum class ETrainingSessionType(val maxRestMinutes: Long) {
    LONG_RUN(5),
    RECOVERY_RUN(10),
    SPEED_WORK(3),
    REST(Long.MAX_VALUE);

    fun canRestWithin(minutes: Long): Boolean {
        return minutes <= maxRestMinutes
    }
}