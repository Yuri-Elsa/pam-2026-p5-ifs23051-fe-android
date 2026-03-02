package org.delcom.pam_p5_ifs23051.helper

import androidx.compose.runtime.MutableState

enum class AlertType(val title: String) {
    ERROR("Error"),
    SUCCESS("Success"),
    INFO("Info"),
    WARNING("Warning")
}

data class AlertState(
    val isVisible: Boolean = false,
    val type: AlertType = AlertType.INFO,
    val message: String = ""
)

object AlertHelper {

    fun show(
        currentState: MutableState<AlertState>,
        type: AlertType,
        message: String
    ) {
        currentState.value = AlertState(
            isVisible = true,
            type = type,
            message = message
        )
    }

    fun dismiss(currentState: MutableState<AlertState>) {
        currentState.value = currentState.value.copy(isVisible = false)
    }
}