package org.delcom.pam_p5_ifs23051.ui.viewmodels

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthLogin
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthLogout
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthRefreshToken
import org.delcom.pam_p5_ifs23051.network.todos.data.RequestAuthRegister
import org.delcom.pam_p5_ifs23051.network.todos.data.ResponseAuthLogin
import org.delcom.pam_p5_ifs23051.network.todos.service.ITodoRepository
import org.delcom.pam_p5_ifs23051.prefs.AuthTokenPref
import javax.inject.Inject

sealed interface AuthUIState {
    data class Success(val data: ResponseAuthLogin) : AuthUIState
    data class Error(val message: String) : AuthUIState
    object Loading : AuthUIState
}

sealed interface AuthActionUIState {
    data class Success(val message: String) : AuthActionUIState
    data class Error(val message: String) : AuthActionUIState
    object Loading : AuthActionUIState
}

sealed interface AuthLogoutUIState {
    data class Success(val message: String) : AuthLogoutUIState
    data class Error(val message: String) : AuthLogoutUIState
    object Loading : AuthLogoutUIState
}

data class UIStateAuth(
    val auth: AuthUIState = AuthUIState.Loading,
    var authRegister: AuthActionUIState = AuthActionUIState.Loading,
    var authLogout: AuthLogoutUIState = AuthLogoutUIState.Loading,
    var authRefreshToken: AuthActionUIState = AuthActionUIState.Loading,
)

@HiltViewModel
@Keep
class AuthViewModel @Inject constructor(
    private val repository: ITodoRepository,
    private val authTokenPref: AuthTokenPref
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateAuth())
    val uiState = _uiState.asStateFlow()

    fun register(
        name: String,
        username: String,
        password: String,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    authRegister = AuthActionUIState.Loading
                )
            }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.postRegister(
                        RequestAuthRegister(
                            name = name,
                            username = username,
                            password = password,
                        )
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            AuthActionUIState.Success(it.data!!.userId)
                        } else {
                            AuthActionUIState.Error(it.message)
                        }
                    },
                    onFailure = {
                        AuthActionUIState.Error(it.message ?: "Unknown error")
                    }
                )

                it.copy(
                    authRegister = tmpState
                )
            }
        }
    }

    fun login(
        username: String,
        password: String,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    auth = AuthUIState.Loading
                )
            }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.postLogin(
                        RequestAuthLogin(
                            username = username,
                            password = password,
                        )
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success" && it.data != null) {
                            authTokenPref.saveAuthToken(it.data.authToken)
                            authTokenPref.saveRefreshToken(it.data.refreshToken)
                            AuthUIState.Success(it.data)
                        } else {
                            AuthUIState.Error(it.message)
                        }
                    },
                    onFailure = {
                        AuthUIState.Error(it.message ?: "Unknown error")
                    }
                )

                it.copy(
                    auth = tmpState
                )
            }
        }
    }

    fun logout(
        authToken: String,
    ) {
        viewModelScope.launch {
            authTokenPref.clearAuthToken()
            authTokenPref.clearRefreshToken()
            _uiState.update {
                it.copy(
                    authLogout = AuthLogoutUIState.Loading
                )
            }
            _uiState.update { it ->
                val tmpState = runCatching {
                    repository.postLogout(
                        RequestAuthLogout(
                            authToken = authToken
                        )
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            AuthLogoutUIState.Success(it.message)
                        } else {
                            AuthLogoutUIState.Error(it.message)
                        }
                    },
                    onFailure = {
                        AuthLogoutUIState.Error(it.message ?: "Unknown error")
                    }
                )

                it.copy(
                    authLogout = tmpState
                )
            }
        }
    }

    fun refreshToken(
        authToken: String,
        refreshToken: String,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    auth = AuthUIState.Loading,
                    authRefreshToken = AuthActionUIState.Loading
                )
            }
            _uiState.update { it ->
                var tmpStateAuth: AuthUIState = AuthUIState.Loading
                var tmpStateAuthRefreshToken: AuthActionUIState = AuthActionUIState.Loading

                runCatching {
                    repository.postRefreshToken(
                        RequestAuthRefreshToken(
                            authToken = authToken,
                            refreshToken = refreshToken,
                        )
                    )
                }.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            authTokenPref.saveAuthToken(it.data!!.authToken)
                            authTokenPref.saveRefreshToken(it.data.refreshToken)
                            tmpStateAuth = AuthUIState.Success(it.data)
                            tmpStateAuthRefreshToken = AuthActionUIState.Success(it.message)
                        } else {
                            tmpStateAuth = AuthUIState.Error(it.message)
                            tmpStateAuthRefreshToken = AuthActionUIState.Error(it.message)
                        }
                    },
                    onFailure = {
                        tmpStateAuth = AuthUIState.Error(it.message ?: "Unknown error")
                        tmpStateAuthRefreshToken = AuthActionUIState.Error(it.message ?: "Unknown error")
                    }
                )

                it.copy(
                    auth = tmpStateAuth,
                    authRefreshToken = tmpStateAuthRefreshToken
                )
            }
        }
    }

    fun loadTokenFromPreferences() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    auth = AuthUIState.Loading
                )
            }

            val authToken = authTokenPref.getAuthToken()
            val refreshToken = authTokenPref.getRefreshToken()

            _uiState.update {
                val loginState = if (authToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
                    AuthUIState.Error("Token tidak tersedia")
                } else {
                    AuthUIState.Success(
                        ResponseAuthLogin(
                            authToken = authToken,
                            refreshToken = refreshToken
                        )
                    )
                }

                it.copy(
                    auth = loginState
                )
            }
        }
    }
}