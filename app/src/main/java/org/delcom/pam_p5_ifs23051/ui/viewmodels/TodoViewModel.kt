package org.delcom.pam_p5_ifs23051.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestUserAbout
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponsePagination
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseStatsData
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23051.network.todos.service.ITodoRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface StatsUIState {
    data class Success(val data: ResponseStatsData) : StatsUIState
    data class Error(val message: String) : StatsUIState
    object Loading : StatsUIState
}

sealed interface TodosUIState {
    data class Success(
        val data: List<ResponseTodoData>,
        val pagination: ResponsePagination
    ) : TodosUIState
    data class Error(val message: String) : TodosUIState
    object Loading : TodosUIState
}

sealed interface TodoUIState {
    data class Success(val data: ResponseTodoData) : TodoUIState
    data class Error(val message: String) : TodoUIState
    object Loading : TodoUIState
}

sealed interface TodoActionUIState {
    data class Success(val message: String) : TodoActionUIState
    data class Error(val message: String) : TodoActionUIState
    object Loading : TodoActionUIState
    object Idle : TodoActionUIState
}

// Separate sealed interface for Home screen todos to avoid shared state race condition
sealed interface HomeTodosUIState {
    data class Success(
        val data: List<ResponseTodoData>,
        val pagination: ResponsePagination
    ) : HomeTodosUIState
    data class Error(val message: String) : HomeTodosUIState
    object Loading : HomeTodosUIState
}

data class UIStateTodo(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val stats: StatsUIState = StatsUIState.Loading,
    val todos: TodosUIState = TodosUIState.Loading,
    var todo: TodoUIState = TodoUIState.Loading,
    var todoAdd: TodoActionUIState = TodoActionUIState.Idle,
    var todoChange: TodoActionUIState = TodoActionUIState.Idle,
    var todoDelete: TodoActionUIState = TodoActionUIState.Idle,
    var todoChangeCover: TodoActionUIState = TodoActionUIState.Loading,
    var profileUpdate: TodoActionUIState = TodoActionUIState.Loading,
    var profilePassword: TodoActionUIState = TodoActionUIState.Loading,
    var profileAbout: TodoActionUIState = TodoActionUIState.Loading,
    var profilePhoto: TodoActionUIState = TodoActionUIState.Idle,
)

@HiltViewModel
@Keep
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateTodo())
    val uiState = _uiState.asStateFlow()

    // Dedicated state for HomeScreen - isolated from uiState to prevent race condition
    private val _homeTodosState = MutableStateFlow<HomeTodosUIState>(HomeTodosUIState.Loading)
    val homeTodosState = _homeTodosState.asStateFlow()

    fun getHomeTodos(authToken: String, page: Int = 1, perPage: Int = 10) {
        viewModelScope.launch {
            _homeTodosState.value = HomeTodosUIState.Loading
            val result = runCatching {
                repository.getTodos(authToken, null, page, perPage, null, null)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success" && response.data != null) {
                        val pagination = response.data.pagination ?: ResponsePagination(
                            currentPage = page,
                            perPage = perPage,
                            total = response.data.todos.size.toLong(),
                            totalPages = 1,
                            hasNextPage = false,
                            hasPrevPage = false
                        )
                        HomeTodosUIState.Success(response.data.todos, pagination)
                    } else {
                        HomeTodosUIState.Error(response.message)
                    }
                },
                onFailure = { HomeTodosUIState.Error(it.message ?: "Unknown error") }
            )
            _homeTodosState.value = result
        }
    }

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val result = runCatching { repository.getUserMe(authToken) }.fold(
                onSuccess = {
                    if (it.status == "success" && it.data != null)
                        ProfileUIState.Success(it.data.user)
                    else ProfileUIState.Error(it.message)
                },
                onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profile = result) }
        }
    }

    fun getStats(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(stats = StatsUIState.Loading) }
            val result = runCatching { repository.getTodoStats(authToken) }.fold(
                onSuccess = {
                    if (it.status == "success" && it.data != null)
                        StatsUIState.Success(it.data.stats)
                    else StatsUIState.Error(it.message)
                },
                onFailure = { StatsUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(stats = result) }
        }
    }

    fun getAllTodos(
        authToken: String,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 10,
        isDone: Boolean? = null,
        urgency: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(todos = TodosUIState.Loading) }
            val result = runCatching {
                repository.getTodos(authToken, search, page, perPage, isDone, urgency)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success" && response.data != null) {
                        // FIX: provide a safe fallback pagination in case it's null
                        val pagination = response.data.pagination ?: ResponsePagination(
                            currentPage = page,
                            perPage = perPage,
                            total = response.data.todos.size.toLong(),
                            totalPages = 1,
                            hasNextPage = false,
                            hasPrevPage = false
                        )
                        TodosUIState.Success(response.data.todos, pagination)
                    } else {
                        TodosUIState.Error(response.message)
                    }
                },
                onFailure = { TodosUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todos = result) }
        }
    }

    fun postTodo(authToken: String, title: String, description: String, urgency: String = "medium") {
        viewModelScope.launch {
            _uiState.update { it.copy(todoAdd = TodoActionUIState.Loading) }
            val result = runCatching {
                // FIX: correct argument order — isDone defaults to false
                repository.postTodo(authToken, RequestTodo(title, description, false, urgency))
            }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoAdd = result) }
        }
    }

    fun resetTodoAddState() {
        _uiState.update { it.copy(todoAdd = TodoActionUIState.Idle) }
    }

    fun getTodoById(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todo = TodoUIState.Loading) }
            val result = runCatching { repository.getTodoById(authToken, todoId) }.fold(
                onSuccess = {
                    if (it.status == "success" && it.data != null)
                        TodoUIState.Success(it.data.todo)
                    else TodoUIState.Error(it.message)
                },
                onFailure = { TodoUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todo = result) }
        }
    }

    fun putTodo(
        authToken: String,
        todoId: String,
        title: String,
        description: String,
        isDone: Boolean,
        urgency: String = "medium"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChange = TodoActionUIState.Loading) }
            val result = runCatching {
                repository.putTodo(authToken, todoId, RequestTodo(title, description, isDone, urgency))
            }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoChange = result) }
        }
    }

    fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChangeCover = TodoActionUIState.Loading) }
            val result = runCatching { repository.putTodoCover(authToken, todoId, file) }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoChangeCover = result) }
        }
    }

    fun deleteTodo(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoDelete = TodoActionUIState.Loading) }
            val result = runCatching { repository.deleteTodo(authToken, todoId) }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoDelete = result) }
        }
    }

    fun resetTodoDeleteState() {
        _uiState.update { it.copy(todoDelete = TodoActionUIState.Idle) }
    }

    fun updateProfile(authToken: String, name: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileUpdate = TodoActionUIState.Loading) }
            val result = runCatching {
                repository.putUserMe(authToken, RequestUserChange(name, username))
            }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profileUpdate = result) }
        }
    }

    fun updatePassword(authToken: String, password: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profilePassword = TodoActionUIState.Loading) }
            val result = runCatching {
                repository.putUserMePassword(authToken, RequestUserChangePassword(newPassword, password))
            }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profilePassword = result) }
        }
    }

    fun updateAbout(authToken: String, about: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileAbout = TodoActionUIState.Loading) }
            val result = runCatching {
                repository.putUserMeAbout(authToken, RequestUserAbout(about))
            }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profileAbout = result) }
        }
    }

    fun updatePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(profilePhoto = TodoActionUIState.Loading) }
            val result = runCatching { repository.putUserMePhoto(authToken, file) }.fold(
                onSuccess = {
                    if (it.status == "success") TodoActionUIState.Success(it.message)
                    else TodoActionUIState.Error(it.message)
                },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profilePhoto = result) }
        }
    }

    fun resetProfilePhotoState() {
        _uiState.update { it.copy(profilePhoto = TodoActionUIState.Idle) }
    }
}