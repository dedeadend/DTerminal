package dedeadend.dterminal.ui.terminal

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dedeadend.dterminal.R
import dedeadend.dterminal.core.BaseTopBar
import dedeadend.dterminal.domain.model.Settings
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.model.TerminalState
import dedeadend.dterminal.ui.theme.ErrorTextColor
import dedeadend.dterminal.ui.theme.InfoTextColor
import kotlinx.coroutines.flow.Flow


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun Terminal(
    viewModel: TerminalViewModel = hiltViewModel(),
    terminalCommandChannel: Flow<String>
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxHeight = screenHeight / 3

    LaunchedEffect(Unit) {
        terminalCommandChannel.collect { command ->
            viewModel.onEvent(TerminalUiEvent.OnCommandChange(command))
            viewModel.onEvent(TerminalUiEvent.Execute)
        }
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.maxValue) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

//    val listScrollState = rememberLazyListState()
//    LaunchedEffect(uiState.logs) {
//        if (uiState.canScroll) {
//            yield()
//            listScrollState.animateScrollToItem(0)
//        }
//    }

    Scaffold(
        topBar = {
            TerminalTopBar(viewModel, uiState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp)
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(12.dp),
                        text = uiState.terminalLogText,
                        fontSize = uiState.settings.logFontSize.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = (uiState.settings.logFontSize + 5).sp,
                        textAlign = TextAlign.Left,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
//                LazyColumn(
//                    state = listScrollState,
//                    modifier = Modifier.fillMaxWidth(),
//                    contentPadding = PaddingValues(8.dp),
//                    verticalArrangement = Arrangement.spacedBy(12.dp),
//                    reverseLayout = true
//                ) {
//                    items(
//                        items = uiState.logs,
//                        key = { item -> item.id },
//                        contentType = { item -> item.state }) { item ->
//                        OutputItem(item, uiState.settings)
//                    }
//                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .heightIn(0.dp, maxHeight)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = uiState.command,
                        onValueChange = { viewModel.onEvent(TerminalUiEvent.OnCommandChange(it)) },
                        placeholder = {
                            Text(
                                text = "Enter "
                                        + (if (uiState.isRoot) "#" else "$")
                                        + " commands..."
                            )
                        },
                        maxLines = Int.MAX_VALUE,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = uiState.executionState != TerminalState.Running) {
                                viewModel.onEvent(TerminalUiEvent.Execute)
                            }, contentAlignment = Alignment.Center
                    ) {
                        if (uiState.executionState == TerminalState.Running) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutputItem(terminalLog: TerminalLog, settings: Settings) {
    SelectionContainer {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = if (terminalLog.state == TerminalState.Info)
                "\n\n\n" + terminalLog.date + "\n" + terminalLog.message + "\n"
            else
                terminalLog.message,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = settings.logFontSize.sp,
                lineHeight = (settings.logFontSize + 4).sp,
                textAlign = TextAlign.Left,
                color = when (terminalLog.state) {
                    TerminalState.Info -> {
                        if (settings.logInfoFontColor == -1)
                            InfoTextColor
                        else
                            Color(settings.logInfoFontColor)
                    }

                    TerminalState.Error -> {
                        if (settings.logErrorFontColor == -1)
                            ErrorTextColor
                        else
                            Color(settings.logErrorFontColor)
                    }

                    else -> {
                        if (settings.logSuccessFontColor == -1)
                            MaterialTheme.colorScheme.onSurface
                        else
                            Color(settings.logSuccessFontColor)
                    }
                }
            )
        )
    }
}

@Composable
private fun TerminalTopBar(
    viewmodel: TerminalViewModel,
    uiState: TerminalUiState
) {
    val uriHandler = LocalUriHandler.current
    BaseTopBar(actions = {
        Box(
            modifier = Modifier.padding(0.dp, 24.dp, 0.dp, 0.dp)
        ) {
            IconButton(onClick = { viewmodel.onEvent(TerminalUiEvent.ToggleToolsMenu(true)) })
            {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tools",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            DropdownMenu(

                expanded = uiState.isToolsMenuOpen,
                onDismissRequest = { viewmodel.onEvent(TerminalUiEvent.ToggleToolsMenu(false)) },
                offset = DpOffset(0.dp, 16.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Github") },
                    onClick = {
                        viewmodel.onEvent(TerminalUiEvent.ToggleToolsMenu(false))
                        uriHandler.openUri("https://github.com/dedeadend/dterminal")
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = "Github",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Clear output") },
                    onClick = {
                        viewmodel.onEvent(TerminalUiEvent.ToggleToolsMenu(false))
                        viewmodel.onEvent(TerminalUiEvent.ClearOutput)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Terminate process") },
                    onClick = {
                        viewmodel.onEvent(TerminalUiEvent.ToggleToolsMenu(false))
                        viewmodel.onEvent(TerminalUiEvent.Terminate)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Terminate",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (uiState.isRoot) "Switch to Shell mode" else "Switch to Root mode") },
                    onClick = {
                        viewmodel.onEvent(TerminalUiEvent.ToggleToolsMenu(false))
                        viewmodel.onEvent(TerminalUiEvent.ToggleRoot)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = "Root",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    })
}