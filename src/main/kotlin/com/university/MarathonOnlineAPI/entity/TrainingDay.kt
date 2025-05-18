package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*

@Entity
@Table(name = "training_day")
data class TrainingDay(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: TrainingPlan? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    var session: TrainingSession? = null,

    @Column
    var week: Int? = null,

    @Column(name = "day_of_week")
    var dayOfWeek: Int? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "race_training_date",
        joinColumns = [JoinColumn(name = "training_date_id")],
        inverseJoinColumns = [JoinColumn(name = "race_id")]
    )
    var records: List<Record>? = null,

    @Column
    var status: ETrainingDayStatus?= null
){
    // Constructor to help with bidirectional relationship
    constructor() : this(null, null, null, null, null)

    // After setting the session, update the session's trainingDays collection
    fun setSessionAndUpdateRelationship(session: TrainingSession?) {
        this.session = session
        session?.trainingDays?.add(this)
    }
}

enum class ETrainingDayStatus {
    ACTIVE,
    COMPLETED,
    MISSED
}
