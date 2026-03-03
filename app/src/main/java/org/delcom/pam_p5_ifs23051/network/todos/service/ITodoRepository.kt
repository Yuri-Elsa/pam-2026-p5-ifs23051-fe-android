package org.delcom.pam_p5_ifs23051.network.todos.service

import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23051.network.data.ResponseMessage
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthLogin
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthLogout
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthRefreshToken
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthRegister
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestUserAbout
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseAuthLogin
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseAuthRegister
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseStats
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodo
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodoAdd
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodosPaginated
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseUser

interface ITodoRepository {

    // ----------------------------------
    // Auth
    // ----------------------------------

    suspend fun postRegister(
        request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    suspend fun postLogin(
        request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    suspend fun postLogout(
        request: RequestAuthLogout
    ): ResponseMessage<String?>

    suspend fun postRefreshToken(
        request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ----------------------------------
    // Users
    // ----------------------------------

    suspend fun getUserMe(
        authToken: String
    ): ResponseMessage<ResponseUser?>

    suspend fun putUserMe(
        authToken: String,
        request: RequestUserChange
    ): ResponseMessage<String?>

    suspend fun putUserMePassword(
        authToken: String,
        request: RequestUserChangePassword
    ): ResponseMessage<String?>

    suspend fun putUserMePhoto(
        authToken: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    suspend fun putUserMeAbout(
        authToken: String,
        request: RequestUserAbout
    ): ResponseMessage<String?>

    // ----------------------------------
    // Todos
    // ----------------------------------

    suspend fun getTodos(
        authToken: String,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 10,
        isDone: Boolean? = null,
        urgency: String? = null
    ): ResponseMessage<ResponseTodosPaginated?>

    suspend fun getTodoStats(
        authToken: String
    ): ResponseMessage<ResponseStats?>

    suspend fun postTodo(
        authToken: String,
        request: RequestTodo
    ): ResponseMessage<ResponseTodoAdd?>

    suspend fun getTodoById(
        authToken: String,
        todoId: String
    ): ResponseMessage<ResponseTodo?>

    suspend fun putTodo(
        authToken: String,
        todoId: String,
        request: RequestTodo
    ): ResponseMessage<String?>

    suspend fun putTodoCover(
        authToken: String,
        todoId: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    suspend fun deleteTodo(
        authToken: String,
        todoId: String
    ): ResponseMessage<String?>
}
