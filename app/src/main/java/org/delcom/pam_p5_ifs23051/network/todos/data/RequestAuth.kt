package org.delcom.pam_p5_ifs23051.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestAuthRegister (
    val name: String,
    val username: String,
    val password: String
)

@Serializable
data class RequestAuthLogin (
    val username: String,
    val password: String
)

@Serializable
data class RequestAuthLogout (
    val authToken: String
)

@Serializable
data class RequestAuthRefreshToken (
    val authToken: String,
    val refreshToken: String,
)