package com.university.MarathonOnlineAPI.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "rule")
data class Rule (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var icon: String? = null,
    var name: String? = null,
    var description: String? = null,
    var updateDate: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    @JsonBackReference
    var contest: Contest? = null,
)
