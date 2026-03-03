package org.delcom.pam_p5_ifs23051.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseUser(
    val user: ResponseUserData
)

@Serializable
data class ResponseUserData(
    val id: String,
    val name: String,
    val username: String,
    val about: String? = null,
    val createdAt: String,
    val updatedAt: String
)
