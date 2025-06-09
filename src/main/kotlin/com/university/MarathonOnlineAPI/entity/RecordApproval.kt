package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*

@Entity
@Table(name = "record_approval")
data class RecordApproval (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var approvalStatus: ERecordApprovalStatus? = null,
    var fraudRisk: Double? = null,
    var fraudType: String? = null,
    var reviewNote: String? = null,

    @OneToMany(mappedBy = "approval", cascade = [CascadeType.ALL])
    var feedbacks: List<Feedback> = mutableListOf()
)

enum class ERecordApprovalStatus {
    PENDING, APPROVED, REJECTED
}