package com.university.MarathonOnlineAPI.repos

import com.university.MarathonOnlineAPI.entity.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CrudRepository<User, Long> {

}