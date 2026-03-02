package org.delcom.pam_p5_ifs23051.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23051.helper.ConstHelper
import org.delcom.pam_p5_ifs23051.helper.RouteHelper
import org.delcom.pam_p5_ifs23051.ui.theme.DelcomTheme

data class TopAppBarMenuItem(
    val text: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: (() -> Unit)? = null,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    navController: NavHostController,
    title: String = "Home",
    showBackButton: Boolean = true,
    showMenu: Boolean = true,
    customMenuItems: List<TopAppBarMenuItem>? = null,
    onBackClick: (() -> Unit)? = null,
    elevation: Int = 4,
    withSearch: Boolean = false,
    searchQuery: TextFieldValue = TextFieldValue(""),
    onSearchQueryChange: (TextFieldValue) -> Unit = {},
    onSearchAction: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val queryFocusRequester = remember { FocusRequester() }

    fun setExpandState(state: Boolean) {
        expanded = state
    }

    fun setSearchState(state: Boolean) {
        isSearching = state
    }


    // Menu items default
    val defaultMenuItems = listOf(
        TopAppBarMenuItem(
            text = "Profile",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
    )

    val menuItems = customMenuItems ?: defaultMenuItems

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = elevation.dp
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (showBackButton && !isSearching) {
                        // Back button dengan efek visual
                        Card(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(15.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.2f
                                ),
                            ),
                            onClick = {
                                onBackClick?.invoke() ?: RouteHelper.back(navController)
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    if (isSearching) {
                        // Search field
                        TextField(
                            value = searchQuery,
                            onValueChange = { onSearchQueryChange(it) },
                            placeholder = {
                                Text(
                                    "Cari...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(queryFocusRequester),

                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),

                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    onSearchAction()
                                }
                            ),

                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(
                                    alpha = 0.5f
                                ),
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        SideEffect {
                            queryFocusRequester.requestFocus()
                        }
                    } else {
                        // Title
                        Box(
                            modifier = Modifier
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }


                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            actions = {
                if (isSearching) {
                    // Close search button
                    Box(
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                setSearchState(false)
                                onSearchQueryChange(TextFieldValue(""))
                                onSearchAction()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup pencarian",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Search button
                    if (withSearch) {
                        Box(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    setSearchState(true)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Cari",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Menu Button
                    if (showMenu) {
                        Box(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = { expanded = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Menu",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Dropdown menu dengan enhanced design
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                setExpandState(false)
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(end = 8.dp)
                        ) {
                            // Menu items
                            menuItems.forEachIndexed { index, item ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.text,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (item.isDestructive)
                                                    MaterialTheme.colorScheme.error
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text(
                                                text = item.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (item.isDestructive)
                                                    MaterialTheme.colorScheme.error
                                                else
                                                    MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (item.isDestructive) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    },
                                    onClick = {
                                        setExpandState(false)
                                        if (item.route != null) {
                                            RouteHelper.to(
                                                navController,
                                                item.route
                                            )
                                        }
                                        item.onClick?.invoke()
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )

                                // Divider sebelum item terakhir jika destructive
                                if (index == menuItems.size - 2 && menuItems.last().isDestructive) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}


// Preview functions
@Preview(showBackground = true, name = "Default - Light Mode")
@Composable
fun PreviewTopAppBarWithBackComponent() {
    DelcomTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TopAppBarComponent(
                    navController = NavHostController(LocalContext.current),
                    title = "Profile"
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewTopAppBarWithBackComponentDark() {
    DelcomTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TopAppBarComponent(
                    navController = NavHostController(LocalContext.current),
                    title = "Profile Settings"
                )
            }
        }
    }
}

// Contoh penggunaan dengan custom menu
@Preview(showBackground = true, name = "Custom Menu")
@Composable
fun PreviewTopAppBarCustomMenu() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            val customItems = listOf(
                TopAppBarMenuItem(
                    text = "Dashboard",
                    icon = Icons.Filled.Person,
                    onClick = { /* Custom action */ }
                ),
                TopAppBarMenuItem(
                    text = "Notifications",
                    icon = Icons.Filled.Settings,
                    onClick = { /* Custom action */ }
                ),
                TopAppBarMenuItem(
                    text = "Help & Support",
                    icon = Icons.Filled.Key,
                    onClick = { /* Custom action */ }
                )
            )

            TopAppBarComponent(
                navController = NavHostController(LocalContext.current),
                title = "Custom Menu",
                customMenuItems = customItems
            )
        }
    }
}