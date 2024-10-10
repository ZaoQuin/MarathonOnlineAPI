package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "contest")
data class Contest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    val organizer: User? = null,
    val name: String? = null,
    val desc: String? = null,
    val distance: Double? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val fee: BigDecimal? = null,
    val maxMembers: Int? = null,
    val status: EContestStatus? = null,
    val createDate: LocalDateTime? = null,
    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val rules: List<Rule>? = null,
    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val rewards: List<Reward>? = null,
    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val registrations: List<Registration>? = null,
    val registrationDeadline: LocalDateTime? = null
)

enum class EContestStatus {
    PENDING, ONGOING, FINISHED, CANCELLED, NOT_APPROVAL, DELETED
}
