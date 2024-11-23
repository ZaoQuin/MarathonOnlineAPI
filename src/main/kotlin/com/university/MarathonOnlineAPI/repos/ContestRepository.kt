package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Contest
import com.university.MarathonOnlineAPI.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContestRepository : JpaRepository<Contest, Long> {
    fun findByName(name: String): Optional<Contest>
}
