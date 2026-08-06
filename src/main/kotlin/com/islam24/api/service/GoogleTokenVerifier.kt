package com.islam24.api.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.islam24.api.dto.auth.GoogleUser
import com.islam24.api.error.exception.InvalidGoogleTokenException
import org.springframework.stereotype.Service

@Service
class GoogleTokenVerifier {

    private val transport = GoogleNetHttpTransport.newTrustedTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    private val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
        .setAudience(listOf(System.getenv("GOOGLE_CLIENT_ID")))
        .build()

    fun verify(token: String) : GoogleUser {

        val googleIdToken = try {
            verifier.verify(token)
        }catch (_ : IllegalArgumentException){
            throw InvalidGoogleTokenException()
        } ?: throw InvalidGoogleTokenException()

        val payload = googleIdToken.payload

        return GoogleUser(
            googleId = payload.subject,
            name = payload["name"] as String,
            email = payload.email,
            pictureUrl = payload["picture"] as String,
        )

    }

}