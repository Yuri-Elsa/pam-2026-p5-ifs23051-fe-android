package org.delcom.pam_p5_ifs23051.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseAuthRegister (
    val userId: String
)

@Serializable
data class ResponseAuthLogin (
    val authToken: String,
    val refreshToken: String
)