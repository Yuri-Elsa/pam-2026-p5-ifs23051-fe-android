package org.delcom.pam_p5_ifs23051.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class BottomDialogType {
    INFO, SUCCESS, WARNING, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    cancelText: String? = null,
    onCancel: () -> Unit = onDismiss,
    type: BottomDialogType = BottomDialogType.INFO,
    destructiveAction: Boolean = false,
    dismissOnConfirm: Boolean = true,
    dismissOnCancel: Boolean = true
) {
    if (show) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = {
                // Drag handle
                Card(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .width(40.dp)
                        .height(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon berdasarkan tipe dialog
                val (icon, iconColor, iconBackground) = when (type) {
                    BottomDialogType.SUCCESS -> Triple(
                        Icons.Filled.CheckCircle,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                    BottomDialogType.WARNING -> Triple(
                        Icons.Filled.Warning,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                    BottomDialogType.ERROR -> Triple(
                        Icons.Filled.Error,
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.errorContainer
                    )
                    BottomDialogType.INFO -> Triple(
                        Icons.Filled.Info,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                }

                // Icon container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (cancelText != null) {
                        OutlinedButton(
                            onClick = {
                                onCancel()
                                if (dismissOnCancel) onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = cancelText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onConfirm()
                            if (dismissOnConfirm) onDismiss()
                        },
                        modifier = if (cancelText == null) Modifier.fillMaxWidth() else Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (destructiveAction) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            contentColor = if (destructiveAction) MaterialTheme.colorScheme.onError
                            else MaterialTheme.colorScheme.onPrimary,
                        )
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Cancel button saja (jika tidak ada confirm)
                if (cancelText != null && confirmText.isEmpty()) {
                    Button(
                        onClick = {
                            onCancel()
                            if (dismissOnCancel) onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = cancelText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Extension untuk membuat dialog lebih mudah digunakan
@Composable
fun InfoBottomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit = {}
) {
    BottomDialog(
        show = show,
        onDismiss = onDismiss,
        title = title,
        message = message,
        confirmText = confirmText,
        onConfirm = onConfirm,
        cancelText = null,
        type = BottomDialogType.INFO
    )
}

@Composable
fun SuccessBottomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit = {}
) {
    BottomDialog(
        show = show,
        onDismiss = onDismiss,
        title = title,
        message = message,
        confirmText = confirmText,
        onConfirm = onConfirm,
        cancelText = null,
        type = BottomDialogType.SUCCESS
    )
}

@Composable
fun WarningBottomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    cancelText: String = "Batal",
    onCancel: () -> Unit = onDismiss
) {
    BottomDialog(
        show = show,
        onDismiss = onDismiss,
        title = title,
        message = message,
        confirmText = confirmText,
        onConfirm = onConfirm,
        cancelText = cancelText,
        onCancel = onCancel,
        type = BottomDialogType.WARNING
    )
}

@Composable
fun ErrorBottomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit = {},
    cancelText: String? = null,
    onCancel: () -> Unit = onDismiss
) {
    BottomDialog(
        show = show,
        onDismiss = onDismiss,
        title = title,
        message = message,
        confirmText = confirmText,
        onConfirm = onConfirm,
        cancelText = cancelText,
        onCancel = onCancel,
        type = BottomDialogType.ERROR
    )
}

@Composable
fun ConfirmationBottomDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Ya",
    onConfirm: () -> Unit,
    cancelText: String = "Tidak",
    onCancel: () -> Unit = onDismiss,
    destructiveAction: Boolean = false
) {
    BottomDialog(
        show = show,
        onDismiss = onDismiss,
        title = title,
        message = message,
        confirmText = confirmText,
        onConfirm = onConfirm,
        cancelText = cancelText,
        onCancel = onCancel,
        type = if (destructiveAction) BottomDialogType.ERROR else BottomDialogType.INFO,
        destructiveAction = destructiveAction
    )
}

// Preview untuk testing
@Preview(showBackground = true)
@Composable
fun BottomDialogPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Example usages
            InfoBottomDialog(
                show = true,
                onDismiss = {},
                title = "Informasi",
                message = "Ini adalah dialog informasi",
                confirmText = "Mengerti"
            )

            SuccessBottomDialog(
                show = true,
                onDismiss = {},
                title = "Berhasil!",
                message = "Operasi berhasil dilakukan",
                confirmText = "Lanjut"
            )

            WarningBottomDialog(
                show = true,
                onDismiss = {},
                title = "Peringatan",
                message = "Apakah Anda yakin ingin melanjutkan?",
                confirmText = "Ya",
                onConfirm = {},
                cancelText = "Batal"
            )

            ErrorBottomDialog(
                show = true,
                onDismiss = {},
                title = "Terjadi Kesalahan",
                message = "Tidak dapat menyimpan data. Silakan coba lagi.",
                confirmText = "Coba Lagi"
            )

            ConfirmationBottomDialog(
                show = true,
                onDismiss = {},
                title = "Konfirmasi",
                message = "Apakah Anda yakin ingin menghapus item ini?",
                confirmText = "Hapus",
                onConfirm = {},
                destructiveAction = true
            )
        }
    }
}