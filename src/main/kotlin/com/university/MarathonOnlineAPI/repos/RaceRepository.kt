package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Race
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RaceRepository : JpaRepository<Race, Long>
