package com.university.MarathonOnlineAPI.service

import com.university.MarathonOnlineAPI.dto.FeedbackDTO

interface FeedbackService {
    fun createRecordFeedback(recordId: Long, message: String, jwt: String): FeedbackDTO
    fun createRegistrationFeedback(registrationId: Long, message: String, jwt: String): FeedbackDTO
    fun getFeedbacksByRecordId(recordId: Long): List<FeedbackDTO>
    fun getFeedbacksByApprovalId(approvalId: Long): List<FeedbackDTO>
    fun getFeedbacksByRegistrationId(registrationId: Long): List<FeedbackDTO>
    fun deleteFeedback(feedbackId: Long, jwt: String)
    fun updateFeedback(feedbackDTO: FeedbackDTO): FeedbackDTO
    fun getAllFeedbacks(): List<FeedbackDTO>
    fun getFeedbacksByJwt(jwt: String): List<FeedbackDTO>
    fun getFeedbackCount(approvalId: Long): Long
    fun getFeedbacksByApprovalIds(approvalIds: List<Long>): List<FeedbackDTO>
    fun getFeedbackById(id: Long): FeedbackDTO
}