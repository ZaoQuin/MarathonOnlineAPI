package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*

@Entity
@Table(name = "reward")
data class Reward (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String? = null,
    val desc: String? = null,
    val rank: Int? = null,
    val type: ERewardType? = null,
    val isClaim: Boolean? = null,

    @ManyToOne
    @JoinColumn(name = "contest_id")
    val contest: Contest? = null,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration? = null
)

enum class ERewardType {
    PHYSICAL, VIRTUAL
}
