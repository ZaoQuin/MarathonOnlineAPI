package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "race")
data class Race(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val distance: Double? = null,
    val timeTaken: Long? = null,
    val avgSpeed: Double? = null,
    val timestamp: LocalDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration? = null
)
