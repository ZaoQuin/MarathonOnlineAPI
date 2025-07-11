package com.university.MarathonOnlineAPI.controller.user

data class CheckPhoneNumberRespone (
    var exists: Boolean,
    var message: String? = null
)