package com.university.MarathonOnlineAPI.entity

import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "rule")
data class Rule (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val icon: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val updateDate: Date? = null,

    @ManyToOne
    @JoinColumn(name = "contest_id")
    val contest: Contest? = null,
)
