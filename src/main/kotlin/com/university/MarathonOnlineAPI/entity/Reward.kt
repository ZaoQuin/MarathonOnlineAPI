package com.university.MarathonOnlineAPI.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import org.springframework.context.annotation.Description

@Entity
@Table(name = "reward")
data class Reward (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var name: String? = null,
    var description: String? = null,
    var rewardRank: Int? = null,
    var type: ERewardType? = null,
    var isClaim: Boolean? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    @JsonBackReference
    var contest: Contest? = null,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration? = null
)

enum class ERewardType {
    PHYSICAL, VIRTUAL
}
