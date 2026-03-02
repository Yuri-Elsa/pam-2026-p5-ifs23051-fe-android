package org.delcom.pam_p5_ifs23051.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestUserChange (
    val name: String,
    val username: String
)

@Serializable
data class RequestUserChangePassword (
    val newPassword: String,
    val password: String
)