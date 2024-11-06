package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Contest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContestRepository : JpaRepository<Contest, Long>
