package org.delcom.pam_p5_ifs23051.helper

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.delcom.pam_p5_ifs23051.network.data.ResponseMessage
import retrofit2.HttpException

object SuspendHelper {
    enum class SnackBarType(val title: String) {
        ERROR(title = "error"),
        SUCCESS(title = "success"),
        INFO(title = "info"),
        WARNING(title = "warning")
    }

    suspend fun showSnackBar(snackbarHost: SnackbarHostState, type: SnackBarType, message: String) {
        coroutineScope {
            launch {
                snackbarHost.showSnackbar(
                    message = "${type.title}|$message",
                    actionLabel = "Close",
                    duration = SnackbarDuration.Indefinite
                )
            }

            launch {
                delay(5_000)
                snackbarHost.currentSnackbarData?.dismiss()
            }
        }
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> ResponseMessage<T?>): ResponseMessage<T?> {
        return try {
            apiCall()
        } catch (e: HttpException) {
            val errorMessage = parseHttpErrorMessage(e)
            ResponseMessage(
                status = "error",
                message = errorMessage
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseMessage(
                status = "error",
                message = e.message ?: "Unknown error"
            )
        }
    }

    private fun parseHttpErrorMessage(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()

            if (errorBody.isNullOrBlank()) {
                return "Server error (${e.code()})"
            }

            // Coba parse sebagai JSON
            val jsonError = Gson().fromJson(errorBody, ResponseMessage::class.java)
            jsonError?.message ?: "Server error (${e.code()})"

        } catch (jsonEx: JsonSyntaxException) {
            // Server return bukan JSON (HTML, plain text, dll)
            "Server error (${e.code()})"
        } catch (ex: Exception) {
            "Server error (${e.code()})"
        }
    }
}