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
}

data class UIStateTodo(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val stats: StatsUIState = StatsUIState.Loading,
    val todos: TodosUIState = TodosUIState.Loading,
    var todo: TodoUIState = TodoUIState.Loading,
    var todoAdd: TodoActionUIState = TodoActionUIState.Loading,
    var todoChange: TodoActionUIState = TodoActionUIState.Loading,
    var todoDelete: TodoActionUIState = TodoActionUIState.Loading,
    var todoChangeCover: TodoActionUIState = TodoActionUIState.Loading,
    var profileUpdate: TodoActionUIState = TodoActionUIState.Loading,
    var profilePassword: TodoActionUIState = TodoActionUIState.Loading,
    var profileAbout: TodoActionUIState = TodoActionUIState.Loading,
    var profilePhoto: TodoActionUIState = TodoActionUIState.Loading,
)

@HiltViewModel
@Keep
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateTodo())
    val uiState = _uiState.asStateFlow()

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.getUserMe(authToken)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") ProfileUIState.Success(it.data!!.user)
                        else ProfileUIState.Error(it.message)
                    },
                    onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(profile = tmpState)
            }
        }
    }

    fun getStats(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(stats = StatsUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.getTodoStats(authToken)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") StatsUIState.Success(it.data!!.stats)
                        else StatsUIState.Error(it.message)
                    },
                    onFailure = { StatsUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(stats = tmpState)
            }
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
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.getTodos(authToken, search, page, perPage, isDone, urgency)
                }.fold(
                    onSuccess = {
                        if (it.status == "success")
                            TodosUIState.Success(it.data!!.todos, it.data.pagination)
                        else TodosUIState.Error(it.message)
                    },
                    onFailure = { TodosUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(todos = tmpState)
            }
        }
    }

    fun postTodo(authToken: String, title: String, description: String, urgency: String = "medium") {
        viewModelScope.launch {
            _uiState.update { it.copy(todoAdd = TodoActionUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.postTodo(authToken, RequestTodo(title, description, urgency = urgency))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(todoAdd = tmpState)
            }
        }
    }

    fun getTodoById(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todo = TodoUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.getTodoById(authToken, todoId)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoUIState.Success(it.data!!.todo)
                        else TodoUIState.Error(it.message)
                    },
                    onFailure = { TodoUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(todo = tmpState)
            }
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
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.putTodo(authToken, todoId, RequestTodo(title, description, isDone, urgency))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(todoChange = tmpState)
            }
        }
    }

    fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChangeCover = TodoActionUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.putTodoCover(authToken, todoId, file)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(todoChangeCover = tmpState)
            }
        }
    }

    fun deleteTodo(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoDelete = TodoActionUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.deleteTodo(authToken, todoId)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(todoDelete = tmpState)
            }
        }
    }

    fun updateProfile(authToken: String, name: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileUpdate = TodoActionUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.putUserMe(authToken, RequestUserChange(name, username))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(profileUpdate = tmpState)
            }
        }
    }

    fun updatePassword(authToken: String, password: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profilePassword = TodoActionUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.putUserMePassword(authToken, RequestUserChangePassword(newPassword, password))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(profilePassword = tmpState)
            }
        }
    }

    fun updateAbout(authToken: String, about: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileAbout = TodoActionUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.putUserMeAbout(authToken, RequestUserAbout(about))
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(profileAbout = tmpState)
            }
        }
    }

    fun updatePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(profilePhoto = TodoActionUIState.Loading) }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.putUserMePhoto(authToken, file)
                }.fold(
                    onSuccess = {
                        if (it.status == "success") TodoActionUIState.Success(it.message)
                        else TodoActionUIState.Error(it.message)
                    },
                    onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                )
                it.copy(profilePhoto = tmpState)
            }
        }
    }
}
