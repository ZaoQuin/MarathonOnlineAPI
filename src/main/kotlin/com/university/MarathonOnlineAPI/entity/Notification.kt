package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notification")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    val receiver: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    val contest: Contest? = null,
    val title: String? = null,
    val content: String? = null,
    val createAt: LocalDateTime? = null,
    val isRead: Boolean? = null,
    val type: ENotificationType? = null
)

enum class ENotificationType {
    REWARD, NEW_NOTIFICATION
}
