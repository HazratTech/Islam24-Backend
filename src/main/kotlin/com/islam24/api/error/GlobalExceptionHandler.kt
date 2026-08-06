package com.islam24.api.error

import com.islam24.api.error.exception.InvalidGoogleTokenException
import com.islam24.api.error.exception.InvalidRefreshTokenException
import com.islam24.api.error.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFoundException(e: UserNotFoundException): ErrorResponse {
        return ErrorResponse(
            error = e.message ?: "User not found",
        )
    }

    @ExceptionHandler(InvalidGoogleTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidGoogleTokenException(): ErrorResponse {
        return ErrorResponse(
            error = "Invalid google token"
        )
    }

    @ExceptionHandler(InvalidRefreshTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidRefreshTokenException(): ErrorResponse {
        return ErrorResponse(
            error = "Invalid refresh token"
        )
    }

}