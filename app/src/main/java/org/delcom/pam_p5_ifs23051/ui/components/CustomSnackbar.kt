package org.delcom.pam_p5_ifs23051.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    onDismiss: () -> Unit
) {
    val rawMessage = snackbarData.visuals.message
    val splitMessage = rawMessage.split("|", limit = 2)
    val type = splitMessage.getOrNull(0) ?: "info"
    val message = splitMessage.getOrNull(1) ?: rawMessage

    val (icon, iconColor, bgColor) = when (type) {
        "warning" -> Triple(Icons.Default.Warning, Color(0xFFFFAE1F), Color(0xFFFFF5E3))
        "error" -> Triple(Icons.Default.Error, Color(0xFFFA896B), Color(0xFFFFF1ED))
        "success" -> Triple(Icons.Default.CheckCircle, Color(0xFF13DEB9), Color(0xFFE2FBF7))
        else -> Triple(Icons.Default.Info, Color(0xFF539BFF), Color(0xFFEAF3FF))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                color = Color(0xFF383A42),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF383A42)
                )
            }
        }
    }
}