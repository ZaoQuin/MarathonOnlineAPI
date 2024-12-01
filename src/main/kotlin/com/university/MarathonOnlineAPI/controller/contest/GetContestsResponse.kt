package com.university.MarathonOnlineAPI.controller.contest

import com.university.MarathonOnlineAPI.dto.ContestDTO

data class GetContestsResponse(
    val contests: List<ContestDTO>
)
