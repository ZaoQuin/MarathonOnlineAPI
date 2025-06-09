package com.university.MarathonOnlineAPI.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
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
    @JsonManagedReference
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
    var records: List<Record>? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "reward_registration",
        joinColumns = [JoinColumn(name = "registration_id")],
        inverseJoinColumns = [JoinColumn(name = "reward_id")]
    )
    var rewards: List<Reward>? = null,

    var status: ERegistrationStatus? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    @JsonBackReference
    var contest: Contest? = null,

    @OneToMany(mappedBy = "registration", cascade = [CascadeType.ALL])
    var feedbacks: List<Feedback> = mutableListOf()
)

enum class ERegistrationStatus {
    PENDING, ACTIVE, COMPLETED, BLOCK
}
