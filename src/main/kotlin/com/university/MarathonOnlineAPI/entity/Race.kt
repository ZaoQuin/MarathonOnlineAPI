package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "race")
data class Race(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_id")
    var user: User? = null,
    var distance: Double? = null,
    var timeTaken: Long? = null,
    var avgSpeed: Double? = null,
    var timestamp: LocalDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    var registration: Registration? = null
)
