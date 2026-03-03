package org.delcom.pam_p5_ifs23051.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseTodos(
    val todos: List<ResponseTodoData>
)

@Serializable
data class ResponseTodosPaginated(
    val todos: List<ResponseTodoData>,
    val pagination: ResponsePagination
)

@Serializable
data class ResponsePagination(
    val currentPage: Int,
    val perPage: Int,
    val total: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean
)

@Serializable
data class ResponseTodo(
    val todo: ResponseTodoData
)

@Serializable
data class ResponseTodoData(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val description: String,
    val isDone: Boolean = false,
    val urgency: String = "medium",
    val cover: String? = null,
    val createdAt: String = "",
    var updatedAt: String = ""
)

@Serializable
data class ResponseTodoAdd(
    val todoId: String
)
