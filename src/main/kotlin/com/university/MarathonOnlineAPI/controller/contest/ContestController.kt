package com.university.MarathonOnlineAPI.controller.contest

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.university.MarathonOnlineAPI.controller.StringResponse
import com.university.MarathonOnlineAPI.controller.user.CheckEmailResponse
import com.university.MarathonOnlineAPI.dto.ContestDTO
import com.university.MarathonOnlineAPI.exception.ContestException
import com.university.MarathonOnlineAPI.service.ContestService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/contest")
@CrossOrigin("http://localhost:3000/")
class ContestController(private val contestService: ContestService, private val cloudinary: Cloudinary) {

    private val logger = LoggerFactory.getLogger(ContestController::class.java)

    @PostMapping
    fun addContest(@RequestHeader("Authorization") token: String, @RequestBody @Valid createContestRequest: CreateContestRequest): ResponseEntity<Any> {
        return try {
            val jwt = token.replace("Bearer ", "")
            val addedContest = contestService.addContest(createContestRequest, jwt)
            ResponseEntity(addedContest, HttpStatus.OK)
        } catch (e: ContestException) {
            logger.error("Error adding contest: ${e.message}")
            ResponseEntity("Contest error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteContest(@PathVariable id: Long): ResponseEntity<StringResponse> {
        return try {
            contestService.deleteContestById(id)
            logger.info("Contest with ID $id deleted successfully")
            ResponseEntity.ok(StringResponse( str = "Contest with ID $id deleted successfully"))
        } catch (e: ContestException) {
            logger.error("Failed to delete contest with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StringResponse(str = "Failed to delete contest with ID $id: ${e.message}"))
        } catch (e: Exception) {
            logger.error("Failed to delete contest with ID $id: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StringResponse(str = "Failed to delete contest with ID $id: ${e.message}"))
        }
    }

    @PutMapping("/cancel")
    fun cancelContest(@RequestBody @Valid contestDTO: ContestDTO): ResponseEntity<Any> {
        return try {
            val updatedContest = contestService.cancelContest(contestDTO)
            ResponseEntity(updatedContest, HttpStatus.OK)
        } catch (e: ContestException) {
            logger.error("Contest exception: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Error updating contest: ${e.message}")
            throw ContestException("Error updating contest: ${e.message}")
        }
    }

    @PutMapping("/prizes")
    fun awardPrizes(@RequestBody @Valid contestDTO: ContestDTO): ResponseEntity<Any> {
        return try {
            val updatedContest = contestService.awardPrizes(contestDTO)
            ResponseEntity(updatedContest, HttpStatus.OK)
        } catch (e: ContestException) {
            logger.error("Contest exception: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Error updating contest: ${e.message}")
            throw ContestException("Error updating contest: ${e.message}")
        }
    }

    @PutMapping
    fun updateContest(@RequestBody @Valid contestDTO: ContestDTO): ResponseEntity<Any> {
        return try {
            val updatedContest = contestService.updateContest(contestDTO)
            ResponseEntity(updatedContest, HttpStatus.OK)
        } catch (e: ContestException) {
            logger.error("Contest exception: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Error updating contest: ${e.message}")
            throw ContestException("Error updating contest: ${e.message}")
        }
    }

    @PutMapping("/approve/{id}")
    fun approveContest(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val approvedContest = contestService.approveContest(id)
            ResponseEntity.ok(approvedContest)
        } catch (e: ContestException) {
            logger.error("Contest approval error: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error approving contest: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error occurred during contest approval: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while approving the contest")
        }
    }

    @PutMapping("/reject/{id}")
    fun rejectContest(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val rejectedContest = contestService.rejectContest(id)
            ResponseEntity.ok(rejectedContest)
        } catch (e: ContestException) {
            logger.error("Contest approval error: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error approving contest: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error occurred during contest approval: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while approving the contest")
        }
    }


    @GetMapping
    @CrossOrigin(origins = ["http://localhost:3000/"])
    fun getContests(): ResponseEntity<*> {
        return try {
            val contests = contestService.getContests()
            ResponseEntity.ok(GetContestsResponse(contests.ifEmpty { emptyList() }))
        } catch (e: ContestException) {
            logger.error("Contest retrieval error", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error in getContests", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "An unexpected error occurred"))
        }
    }

    @GetMapping("/jwt")
    fun getContestByJwt(@RequestHeader("Authorization") token: String): ResponseEntity<Any>{
        return try {
            val jwt = token.replace("Bearer ", "")
            val contests = contestService.getContestByJwt(jwt)
            ResponseEntity(contests, HttpStatus.OK)
        } catch (e: ContestException) {
            logger.error("Error contest: ${e.message}")
            ResponseEntity("Contest error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/runner")
    fun getContestByRunner(@RequestHeader("Authorization") token: String): ResponseEntity<Any>{
        return try {
            val jwt = token.replace("Bearer ", "")
            val contests = contestService.getContestsByRunner(jwt)
            ResponseEntity.ok(GetContestsResponse(contests))
        } catch (e: ContestException) {
            logger.error("Error contest: ${e.message}")
            ResponseEntity("Contest error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            logger.error("General error occurred: ${e.message}")
            ResponseEntity("Error occurred: ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/check-name")
    fun checkName(@RequestBody request: CheckContestNameRequest): ResponseEntity<Any> {
        val emailExists = contestService.checkNameExist(request.name.trim())
        return if (emailExists) {
            ResponseEntity(CheckEmailResponse(true, "Founded"), HttpStatus.OK)
        } else {
            ResponseEntity(CheckEmailResponse(false, "Not found"), HttpStatus.OK)
        }
    }

    @PostMapping("/check-active")
    fun checkActiveContest(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        val jwt = token.replace("Bearer ", "")
        val exist = contestService.checkActiveContest(jwt)
        return if (exist) {
            ResponseEntity(CheckActiveContestResponse(true, "Founded"), HttpStatus.OK)
        } else {
            ResponseEntity(CheckActiveContestResponse(false, "Not found"), HttpStatus.OK)
        }
    }


    @GetMapping("/home")
    fun getHomeContests(): ResponseEntity<*> {
        return try {
            val contests = contestService.getHomeContests()
            ResponseEntity.ok(GetContestsResponse(contests.ifEmpty { emptyList() }))
        } catch (e: ContestException) {
            logger.error("Contest retrieval error", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error in getContests", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "An unexpected error occurred"))
        }
    }

    @GetMapping("/active-and-finish")
    fun getActiveAndFinish(): ResponseEntity<*> {
        return try {
            val contests = contestService.getActiveAndFinishedAnComepleted()
            ResponseEntity.ok(GetContestsResponse(contests.ifEmpty { emptyList() }))
        } catch (e: ContestException) {
            logger.error("Contest retrieval error", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error in getContests", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "An unexpected error occurred"))
        }
    }

    @GetMapping("/{id}")
    fun getContestById(@PathVariable id: Long): ResponseEntity<ContestDTO> {
        return try {
            val foundContest = contestService.getById(id)
            ResponseEntity.ok(foundContest)
        } catch (e: ContestException) {
            logger.error("Error getting contest by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            logger.error("Error getting contest by ID $id: ${e.message}")
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/{id}/img")
    fun uploadAvatar(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        return try {
            if (file.isEmpty) {
                return ResponseEntity.badRequest().body("File is empty")
            }
            val allowedTypes = listOf("image/jpeg", "image/png", "image/jpg", "image/gif")
            if (!allowedTypes.contains(file.contentType)) {
                return ResponseEntity.badRequest().body("Only image files are allowed")
            }

            if (file.size > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size must be less than 5MB")
            }

            val uploadOptions = ObjectUtils.asMap(
                "folder", "contest_imgs",
                "public_id", "contest_${id}_img",
                "overwrite", true,
                "resource_type", "image"
            )

            val uploadResult = cloudinary.uploader().upload(file.bytes, uploadOptions)
            val imgUrl = uploadResult["secure_url"] as String

            val contest = contestService.getById(id)
            if (contest != null) {
                contest.imgUrl = imgUrl
                contestService.updateContest(contest)
                ResponseEntity.ok(imgUrl)
            } else {
                ResponseEntity.notFound().build()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Upload failed: ${e.message}")
        }
    }
}
