package com.skaknna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.skaknna.viewmodel.BoardViewModel
import com.skaknna.viewmodel.AuthViewModel
import com.skaknna.viewmodel.AuthState
import com.skaknna.data.model.Board
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import com.skaknna.R
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BoardViewModel,
    authViewModel: AuthViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val authState = authViewModel.authState.collectAsState()
    val boards by viewModel.allBoards.collectAsState()

    var boardToRename by remember { mutableStateOf<Board?>(null) }
    var boardToDelete by remember { mutableStateOf<Board?>(null) }
    var showAuthMenu by remember { mutableStateOf(false) }

    if (boardToRename != null) {
        var newName by remember { mutableStateOf(boardToRename!!.name) }
        AlertDialog(
            onDismissRequest = { boardToRename = null },
            title = { Text(stringResource(id = R.string.dashboard_rename_dialog)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(id = R.string.dashboard_new_board_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.renameBoard(boardToRename!!, newName)
                            boardToRename = null
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.button_save), color = com.skaknna.ui.theme.GoldenYellow)
                }
            },
            dismissButton = {
                TextButton(onClick = { boardToRename = null }) {
                    Text(stringResource(id = R.string.button_cancel), color = com.skaknna.ui.theme.WarmWhite)
                }
            },
            containerColor = com.skaknna.ui.theme.WoodDark,
            titleContentColor = com.skaknna.ui.theme.WarmWhite,
            textContentColor = com.skaknna.ui.theme.WarmWhite
        )
    }

    if (boardToDelete != null) {
        AlertDialog(
            onDismissRequest = { boardToDelete = null },
            title = { Text(stringResource(id = R.string.dashboard_delete_dialog)) },
            text = { Text(stringResource(id = R.string.dashboard_delete_confirmation, boardToDelete!!.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBoard(boardToDelete!!)
                        boardToDelete = null
                    }
                ) {
                    Text(stringResource(id = R.string.button_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { boardToDelete = null }) {
                    Text(stringResource(id = R.string.button_cancel), color = com.skaknna.ui.theme.WarmWhite)
                }
            },
            containerColor = com.skaknna.ui.theme.WoodDark,
            titleContentColor = com.skaknna.ui.theme.WarmWhite,
            textContentColor = com.skaknna.ui.theme.WarmWhite
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.screen_title_dashboard), color = com.skaknna.ui.theme.GoldenYellow, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.skaknna.ui.theme.TransparentColor),
                actions = {
                    // Auth Menu
                    Box {
                        IconButton(
                            onClick = { showAuthMenu = !showAuthMenu },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            when (val state = authState.value) {
                                is AuthState.Success -> {
                                    if (state.profilePictureUrl != null) {
                                        // Mostrar foto de perfil de Google
                                        AsyncImage(
                                            model = state.profilePictureUrl,
                                            contentDescription = stringResource(id = R.string.button_profile),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, com.skaknna.ui.theme.WoodDark, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Fallback: mostrar la inicial
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(com.skaknna.ui.theme.LeafGreen)
                                                .border(2.dp, com.skaknna.ui.theme.WoodDark, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                state.profileInitial,
                                                color = com.skaknna.ui.theme.WarmWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = stringResource(id = R.string.button_profile),
                                        tint = com.skaknna.ui.theme.WarmWhite,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showAuthMenu,
                            onDismissRequest = { showAuthMenu = false },
                            modifier = Modifier.background(com.skaknna.ui.theme.WoodDark)
                        ) {
                            when (authState.value) {
                                is AuthState.Success -> {
                                    val userState = authState.value as AuthState.Success
                                    DropdownMenuItem(
                                        text = { Text(userState.email ?: "Usuario", color = com.skaknna.ui.theme.WarmWhite) },
                                        onClick = { showAuthMenu = false }
                                    )
                                    Divider(color = com.skaknna.ui.theme.GoldenYellow.copy(alpha = 0.3f))
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.button_logout), color = com.skaknna.ui.theme.GoldenYellow) },
                                        onClick = {
                                            showAuthMenu = false
                                            authViewModel.signOut()
                                            onNavigateToLogin()
                                        }
                                    )
                                }
                                else -> {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.login_title), color = com.skaknna.ui.theme.GoldenYellow) },
                                        onClick = {
                                            showAuthMenu = false
                                            onNavigateToLogin()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.button_settings),
                            tint = com.skaknna.ui.theme.WarmWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToScanner,
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = stringResource(id = R.string.dashboard_camera_desc)) },
                    text = { Text(stringResource(id = R.string.dashboard_camera_button), fontWeight = FontWeight.Bold) },
                    containerColor = com.skaknna.ui.theme.WoodMedium,
                    contentColor = com.skaknna.ui.theme.GoldenYellow,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                )
                ExtendedFloatingActionButton(
                    onClick = onNavigateToEditor,
                    icon = { Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.dashboard_new_board_desc)) },
                    text = { Text(stringResource(id = R.string.dashboard_new_board_button), fontWeight = FontWeight.Bold) },
                    containerColor = com.skaknna.ui.theme.WoodMedium,
                    contentColor = com.skaknna.ui.theme.GoldenYellow,
                    modifier = Modifier
                        .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(16.dp))
                )
            }
        }
    ) { paddingValues ->
        val dateFormatPattern = stringResource(id = R.string.date_format)
        val dateFormat = SimpleDateFormat(dateFormatPattern, Locale.getDefault())
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 160.dp
            )
        ) {
            if (boards.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.dashboard_no_boards),
                            color = com.skaknna.ui.theme.WarmWhite.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.dashboard_empty_hint),
                            color = com.skaknna.ui.theme.WarmWhite.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(boards, key = { it.id }) { board ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(3.dp, com.skaknna.ui.theme.WoodDark, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.updateFen(board.fen)
                                onNavigateToAnalysis()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = com.skaknna.ui.theme.WoodMedium,
                            contentColor = com.skaknna.ui.theme.WarmWhite
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = board.name, 
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = com.skaknna.ui.theme.GoldenYellow
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.dashboard_saved_format, dateFormat.format(Date(board.updatedAt))), 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = com.skaknna.ui.theme.WarmWhite.copy(alpha = 0.8f)
                                )
                            }
                            
                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(id = R.string.dashboard_options_desc),
                                        tint = com.skaknna.ui.theme.WarmWhite
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(com.skaknna.ui.theme.WoodMedium)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.dashboard_rename_menu), color = com.skaknna.ui.theme.WarmWhite) },
                                        leadingIcon = { 
                                            Icon(
                                                Icons.Default.Edit, 
                                                contentDescription = null,
                                                tint = com.skaknna.ui.theme.GoldenYellow
                                            ) 
                                        },
                                        onClick = {
                                            expanded = false
                                            boardToRename = board
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.dashboard_delete_menu), color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { 
                                            Icon(
                                                Icons.Default.Delete, 
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            ) 
                                        },
                                        onClick = {
                                            expanded = false
                                            boardToDelete = board
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
