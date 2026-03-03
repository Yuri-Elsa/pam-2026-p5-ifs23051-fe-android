package org.delcom.pam_p5_ifs23051.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class UrgencyStyle(val bgColor: Color, val dotColor: Color, val label: String)

private fun urgencyStyle(urgency: String) = when (urgency.lowercase()) {
    "low"  -> UrgencyStyle(Color(0xFFDCFCE7), Color(0xFF16A34A), "Rendah")
    "high" -> UrgencyStyle(Color(0xFFFFE4E4), Color(0xFFDC2626), "Tinggi")
    else   -> UrgencyStyle(Color(0xFFFEF9C3), Color(0xFFCA8A04), "Sedang")
}

/**
 * Badge kecil untuk menampilkan urgency sebuah todo.
 * Dipakai di TodoItemUI dan TodoDetailScreen.
 */
@Composable
fun UrgencyBadge(urgency: String, modifier: Modifier = Modifier) {
    val style = urgencyStyle(urgency)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(style.bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(style.dotColor, CircleShape))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = style.label, color = style.dotColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Selector 3 pilihan urgency: Rendah / Sedang / Tinggi.
 * Dipakai di TodosAddScreen dan TodosEditScreen.
 */
@Composable
fun UrgencySelector(
    selectedUrgency: String,
    onUrgencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("low" to "Rendah", "medium" to "Sedang", "high" to "Tinggi")
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (value, label) ->
            val style = urgencyStyle(value)
            val isSelected = selectedUrgency == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) style.bgColor else MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) style.dotColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onUrgencySelected(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) style.dotColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
