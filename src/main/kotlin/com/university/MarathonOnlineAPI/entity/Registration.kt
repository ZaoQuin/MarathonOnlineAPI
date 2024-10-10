package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Date

@Entity
@Table(name = "registration")
data class Registration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val runner: User? = null,

    @OneToOne(fetch = FetchType.LAZY)
    val payment: Payment? = null,

    val registrationDate: LocalDateTime? = null,
    val completedDate: LocalDateTime? = null,
    val rank: Int? = null,

    @OneToMany(mappedBy = "registration", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val raceResults: List<Race>? = null,

    @OneToMany(mappedBy = "registration", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val rewards: List<Reward>? = null,

    val status: ERegistrationStatus? = null,

    @ManyToOne
    @JoinColumn(name = "contest_id")
    val contest: Contest? = null,
)

enum class ERegistrationStatus {
    PENDING, COMPLETED, REJECTED, CANCELLED
}
