package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "race")
data class Race(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val distance: Double? = null,
    val timeTaken: Long? = null,
    val avgSpeed: Double? = null,
    val timestamp: Date? = null,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration? = null
)
