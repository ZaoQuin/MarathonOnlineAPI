package com.university.MarathonOnlineAPI.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "registration")
data class Registration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var runner: User? = null,

    @OneToOne(fetch = FetchType.LAZY)
    var payment: Payment? = null,

    var registrationDate: LocalDateTime? = null,
    var completedDate: LocalDateTime? = null,
    var registrationRank: Int? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "race_registration",
        joinColumns = [JoinColumn(name = "registration_id")],
        inverseJoinColumns = [JoinColumn(name = "race_id")]
    )
    var races: List<Race>? = null,

    @OneToMany(mappedBy = "registration", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var rewards: List<Reward>? = null,

    var status: ERegistrationStatus? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    @JsonBackReference
    var contest: Contest? = null,
)

enum class ERegistrationStatus {
    PENDING, COMPLETED, REJECTED, CANCELLED
}
