package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.Rule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RuleRepository : JpaRepository<Rule, Long>
