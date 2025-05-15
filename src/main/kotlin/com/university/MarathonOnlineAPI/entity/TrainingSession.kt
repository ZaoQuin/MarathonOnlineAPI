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

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var trainingDays: List<TrainingDay> =  mutableListOf()
)

enum class ETrainingSessionType {
    LONG_RUN, RECOVERY_RUN, SPEED_WORK, REST
}