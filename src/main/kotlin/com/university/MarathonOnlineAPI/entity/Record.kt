package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "record")
data class Record(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runner_id")
    var user: User? = null,
    var steps: Int? = null,
    var distance: Double? = null,
    var avgSpeed: Double? = null,
    var heartRate: Double? = null,
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var source: ERecordSource?= null,

    @ManyToMany(mappedBy = "records")
    var registrations: List<Registration>? = null,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id", referencedColumnName = "id")
    var approval: RecordApproval? = null,
)

enum class ERecordSource {
    DEVICE, THIRD, MERGED
}
