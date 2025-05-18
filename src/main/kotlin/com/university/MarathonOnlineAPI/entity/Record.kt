package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "record")
data class Record(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_id")
    var user: User? = null,
    var steps: Int? = null,
    var distance: Double? = null,
    var timeTaken: Long? = null,
    var avgSpeed: Double? = null,
    var timestamp: LocalDateTime? = null,
    var heartRace: Double? = null,

    @ManyToMany(mappedBy = "records")
    var registrations: List<Registration>? = null,

    @OneToOne
    @JoinColumn(name = "approval_id", referencedColumnName = "id")
    var approval: RecordApproval? = null,

    @ManyToMany(mappedBy = "records")
    var trainingDays: List<TrainingDay>? = null,
)
