package com.skaknna.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.skaknna.data.model.SyncState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.skaknna.R
import com.skaknna.ui.components.SurfaceCard
import com.skaknna.ui.theme.*
import coil.compose.AsyncImage
import com.skaknna.ui.components.AutoSizeText
import com.skaknna.ui.components.SkaknnaTopAppBar

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
    val authState by authViewModel.authState.collectAsState()
    val boards by viewModel.allBoards.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var boardToRename by remember { mutableStateOf<Board?>(null) }
    var boardToDelete by remember { mutableStateOf<Board?>(null) }
    var showAuthMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserId = (authState as? AuthState.Success)?.userId
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            viewModel.syncWithCloud(currentUserId)
        }
    }


    val syncSuccessMsg = stringResource(R.string.dashboard_sync_success)
    val syncErrorMsg   = stringResource(R.string.dashboard_sync_error)
    LaunchedEffect(syncState) {
        when (val s = syncState) {
            is SyncState.Success -> {
                if (s.uploadedCount > 0 || s.downloadedCount > 0) {
                    snackbarHostState.showSnackbar(
                        syncSuccessMsg.format(s.uploadedCount, s.downloadedCount)
                    )
                }
                viewModel.resetSyncState()
            }
            is SyncState.Error -> {
                snackbarHostState.showSnackbar(syncErrorMsg.format(s.message))
                viewModel.resetSyncState()
            }
            else -> Unit
        }
    }

    val isRefreshing = syncState is SyncState.Syncing
    val pullRefreshState = rememberPullToRefreshState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())


    if (boardToRename != null) {
        var newName by remember { mutableStateOf(boardToRename!!.name) }
        AlertDialog(
            onDismissRequest = { boardToRename = null },
            title = { Text(stringResource(id = R.string.dashboard_rename_dialog), color = PrimaryGold, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(id = R.string.dashboard_new_board_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGold,
                        unfocusedBorderColor = OutlineColor,
                        focusedTextColor = WarmWhite,
                        unfocusedTextColor = WarmWhite,
                        cursorColor = PrimaryGold
                    )
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
                    Text(stringResource(id = R.string.button_save), color = PrimaryGold, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { boardToRename = null }) {
                    Text(stringResource(id = R.string.button_cancel), color = WarmWhite)
                }
            },
            containerColor = SurfaceGreen,
            titleContentColor = PrimaryGold,
            textContentColor = WarmWhite
        )
    }


    if (boardToDelete != null) {
        AlertDialog(
            onDismissRequest = { boardToDelete = null },
            title = { Text(stringResource(id = R.string.dashboard_delete_dialog), color = PrimaryGold, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(id = R.string.dashboard_delete_confirmation, boardToDelete!!.name), color = WarmWhite) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBoard(boardToDelete!!)
                        boardToDelete = null
                    }
                ) {
                    Text(stringResource(id = R.string.button_delete), color = ErrorColor, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { boardToDelete = null }) {
                    Text(stringResource(id = R.string.button_cancel), color = WarmWhite)
                }
            },
            containerColor = SurfaceGreen,
            titleContentColor = PrimaryGold,
            textContentColor = WarmWhite
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            SkaknnaTopAppBar(
                title = stringResource(id = R.string.screen_title_dashboard),
                scrollBehavior = scrollBehavior,
                actions = {

                    AnimatedVisibility(
                        visible = isRefreshing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryGold
                        )
                    }


                    Box {
                        IconButton(
                            onClick = { showAuthMenu = !showAuthMenu },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            when (val state = authState) {
                                is AuthState.Success -> {
                                    if (state.profilePictureUrl != null) {
                                        AsyncImage(
                                            model = state.profilePictureUrl,
                                            contentDescription = stringResource(id = R.string.button_profile),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, OutlineColor, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(LeafGreen)
                                                .border(2.dp, OutlineColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                state.profileInitial,
                                                color = WarmWhite,
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
                                        tint = WarmWhite,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showAuthMenu,
                            onDismissRequest = { showAuthMenu = false },
                            modifier = Modifier.background(SurfaceGreen)
                        ) {
                            when (val s = authState) {
                                is AuthState.Success -> {
                                    DropdownMenuItem(
                                        text = { Text(s.email ?: stringResource(R.string.dashboard_user_fallback), color = WarmWhite) },
                                        onClick = { showAuthMenu = false }
                                    )
                                    HorizontalDivider(color = OutlineColor)
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.button_logout), color = PrimaryGold) },
                                        onClick = {
                                            showAuthMenu = false
                                            authViewModel.signOut()
                                            onNavigateToLogin()
                                        }
                                    )
                                }
                                else -> {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.login_title), color = PrimaryGold) },
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
                            tint = PrimaryGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secondary discreet FAB
                ExtendedFloatingActionButton(
                    onClick = onNavigateToEditor,
                    icon = { Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.dashboard_new_board_desc)) },
                    text = { Text(stringResource(id = R.string.dashboard_new_board_button), fontWeight = FontWeight.Medium) },
                    containerColor = SurfaceGreen,
                    contentColor = WarmWhite.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                )
                
                // Primary protagonist FAB
                LargeFloatingActionButton(
                    onClick = onNavigateToScanner,
                    containerColor = PrimaryGold,
                    contentColor = DeepEspresso,
                    shape = RoundedCornerShape(24.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = stringResource(id = R.string.dashboard_camera_desc),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        val dateFormatPattern = stringResource(id = R.string.date_format)
        val dateFormat = SimpleDateFormat(dateFormatPattern, Locale.getDefault())

        val radialGradient = Brush.radialGradient(
            colors = listOf(BackgroundGradientCenter, BackgroundGradientEdge),
            radius = Float.MAX_VALUE
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                val uid = (authState as? AuthState.Success)?.userId
                if (uid != null) {
                    viewModel.syncWithCloud(uid)
                }
            },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .background(radialGradient)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
                                color = WarmWhite.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.dashboard_empty_hint),
                                color = WarmWhite.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyMedium
                            )


                            if (currentUserId != null) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = PrimaryGold.copy(alpha = 0.6f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.dashboard_sync_pull_hint),
                                        color = PrimaryGold.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(boards, key = { it.id }) { board ->
                        var expanded by remember { mutableStateOf(false) }

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.updateFen(board.fen)
                                    onNavigateToAnalysis()
                                },
                            shape = RoundedCornerShape(28.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineColor),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = Color.Transparent,
                                contentColor = WarmWhite
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    AutoSizeText(
                                        text = board.name,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 22.sp
                                        ),
                                        color = PrimaryGold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.dashboard_saved_format, dateFormat.format(Date(board.updatedAt))),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WarmWhite.copy(alpha = 0.8f)
                                    )
                                }

                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(id = R.string.dashboard_options_desc),
                                            tint = WarmWhite
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(SurfaceGreen)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.dashboard_rename_menu), color = WarmWhite) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = PrimaryGold
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                boardToRename = board
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.dashboard_delete_menu), color = ErrorColor) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = ErrorColor
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
}
