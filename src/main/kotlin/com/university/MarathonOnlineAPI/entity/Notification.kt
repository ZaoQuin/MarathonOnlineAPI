package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notification")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    var receiver: User? = null,
    var objectId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var createAt: LocalDateTime? = null,
    var isRead: Boolean? = null,
    var type: ENotificationType? = null
)

enum class ENotificationType {
    REWARD, NEW_CONTEST, BLOCK_CONTEST, ACCEPT_CONTEST, NOT_APPROVAL_CONTEST, REJECTED_RECORD
}
