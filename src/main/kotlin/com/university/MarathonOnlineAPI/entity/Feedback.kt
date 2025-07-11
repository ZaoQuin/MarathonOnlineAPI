package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "feedback")
data class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User,

    @Column(nullable = false, columnDefinition = "TEXT")
    var message: String,

    @Column(nullable = false)
    var sentAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id")
    var approval: RecordApproval?= null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    var registration: Registration?= null,
)
