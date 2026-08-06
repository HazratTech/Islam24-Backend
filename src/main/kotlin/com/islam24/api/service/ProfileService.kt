package com.islam24.api.service

import com.islam24.api.entity.User
import com.islam24.api.error.exception.UserNotFoundException
import com.islam24.api.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class ProfileService(private val userRepository: UserRepository) {



    private fun findUserById(id: UUID): User? {
        return userRepository.findById(id).orElseThrow {
            UserNotFoundException(id = id)
        }
    }

}