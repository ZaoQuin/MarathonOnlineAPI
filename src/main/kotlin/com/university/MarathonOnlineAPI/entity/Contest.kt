package com.university.MarathonOnlineAPI.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "contest")
data class Contest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id")
    var organizer: User? = null,
    var name: String? = null,
    var description: String? = null,
    var distance: Double? = null,
    var startDate: LocalDateTime? = null,
    var endDate: LocalDateTime? = null,
    var fee: BigDecimal? = null,
    var maxMembers: Int? = null,
    var status: EContestStatus? = null,
    var createDate: LocalDateTime? = null,
    @OneToMany(mappedBy = "contest", fetch = FetchType.EAGER , cascade = [CascadeType.ALL])
    @JsonManagedReference
    var rules: List<Rule>? = null,
    @OneToMany(mappedBy = "contest", fetch = FetchType.EAGER , cascade = [CascadeType.ALL])
    @JsonManagedReference
    var rewards: List<Reward>? = null,
    @OneToMany(mappedBy = "contest", fetch = FetchType.EAGER , cascade = [CascadeType.ALL])
    @JsonManagedReference
    var registrations: List<Registration>? = null,
    var registrationDeadline: LocalDateTime? = null
)

enum class EContestStatus {
    PENDING, ONGOING, FINISHED, CANCELLED, NOT_APPROVAL, DELETED
}
