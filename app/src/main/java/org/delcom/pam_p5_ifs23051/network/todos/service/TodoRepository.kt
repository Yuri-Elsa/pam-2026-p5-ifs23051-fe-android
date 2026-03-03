package org.delcom.pam_p5_ifs23051.network.todos.service

import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23051.helper.SuspendHelper
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

class TodoRepository(
    private val apiService: TodoApiService
) : ITodoRepository {

    // ----------------------------------
    // Auth
    // ----------------------------------

    override suspend fun postRegister(
        request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?> {
        return SuspendHelper.safeApiCall { apiService.postRegister(request) }
    }

    override suspend fun postLogin(
        request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?> {
        return SuspendHelper.safeApiCall { apiService.postLogin(request) }
    }

    override suspend fun postLogout(
        request: RequestAuthLogout
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.postLogout(request) }
    }

    override suspend fun postRefreshToken(
        request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?> {
        return SuspendHelper.safeApiCall { apiService.postRefreshToken(request) }
    }

    // ----------------------------------
    // Users
    // ----------------------------------

    override suspend fun getUserMe(
        authToken: String
    ): ResponseMessage<ResponseUser?> {
        return SuspendHelper.safeApiCall { apiService.getUserMe("Bearer $authToken") }
    }

    override suspend fun putUserMe(
        authToken: String,
        request: RequestUserChange
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.putUserMe("Bearer $authToken", request) }
    }

    override suspend fun putUserMePassword(
        authToken: String,
        request: RequestUserChangePassword
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.putUserMePassword("Bearer $authToken", request) }
    }

    override suspend fun putUserMePhoto(
        authToken: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.putUserMePhoto("Bearer $authToken", file) }
    }

    override suspend fun putUserMeAbout(
        authToken: String,
        request: RequestUserAbout
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.putUserMeAbout("Bearer $authToken", request) }
    }

    // ----------------------------------
    // TODOS
    // ----------------------------------

    override suspend fun getTodoStats(
        authToken: String
    ): ResponseMessage<ResponseStats?> {
        return SuspendHelper.safeApiCall { apiService.getTodoStats("Bearer $authToken") }
    }

    override suspend fun getTodos(
        authToken: String,
        search: String?,
        page: Int,
        perPage: Int,
        isDone: Boolean?,
        urgency: String?
    ): ResponseMessage<ResponseTodosPaginated?> {
        return SuspendHelper.safeApiCall {
            apiService.getTodos("Bearer $authToken", search, page, perPage, isDone, urgency)
        }
    }

    override suspend fun postTodo(
        authToken: String,
        request: RequestTodo
    ): ResponseMessage<ResponseTodoAdd?> {
        return SuspendHelper.safeApiCall { apiService.postTodo("Bearer $authToken", request) }
    }

    override suspend fun getTodoById(
        authToken: String,
        todoId: String
    ): ResponseMessage<ResponseTodo?> {
        return SuspendHelper.safeApiCall { apiService.getTodoById("Bearer $authToken", todoId) }
    }

    override suspend fun putTodo(
        authToken: String,
        todoId: String,
        request: RequestTodo
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.putTodo("Bearer $authToken", todoId, request) }
    }

    override suspend fun putTodoCover(
        authToken: String,
        todoId: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.putTodoCover("Bearer $authToken", todoId, file) }
    }

    override suspend fun deleteTodo(
        authToken: String,
        todoId: String
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall { apiService.deleteTodo("Bearer $authToken", todoId) }
    }
}
