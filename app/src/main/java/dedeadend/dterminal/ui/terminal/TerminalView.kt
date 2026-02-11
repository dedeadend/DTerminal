package dedeadend.dterminal.ui.terminal

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dedeadend.dterminal.domin.TerminalLog
import dedeadend.dterminal.domin.TerminalState
import dedeadend.dterminal.ui.theme.terminalErrorTextStyle
import dedeadend.dterminal.ui.theme.terminalInfoTextStyle
import dedeadend.dterminal.ui.theme.terminalSuccessTextStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.yield


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun Terminal(viewModel: TerminalViewModel = hiltViewModel(), terminalCommand: Flow<String>) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxHeight = screenHeight / 3
    val scrollState = rememberLazyListState()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        terminalCommand.collect { command ->
            viewModel.onCommandChange(command)
            viewModel.execute()
        }
    }

    LaunchedEffect(logs) {
        if (!logs.isEmpty()) {
            yield()
            scrollState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TerminalTopBar(
                viewModel,
                onMenuClick = { viewModel.toggleToolsMenu(true) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    8.dp,
                    paddingValues.calculateTopPadding(),
                    8.dp,
                    8.dp
                )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp, 0.dp, 8.dp, 8.dp),
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    reverseLayout = true
                ) {
                    items(items = logs, key = { item -> item.id }) { item ->
                        OutputItem(item)
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .heightIn(0.dp, maxHeight)
                    .padding(8.dp),
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
                        value = viewModel.command,
                        onValueChange = { viewModel.onCommandChange(it) },
                        placeholder = {
                            Text(
                                text = "Enter "
                                        + (if (viewModel.isRoot) "#" else "$")
                                        + " Commands..."
                            )
                        },
                        maxLines = Int.MAX_VALUE
                    )
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(enabled = viewModel.state != TerminalState.Running) {
                                viewModel.execute()
                            }, contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.state == TerminalState.Running) {
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
private fun OutputItem(output: TerminalLog) {
    SelectionContainer {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Left,
            text = terminalLog2String(output),
            style = when (output.state) {
                TerminalState.Info -> terminalInfoTextStyle
                TerminalState.Error -> terminalErrorTextStyle
                else -> terminalSuccessTextStyle
            }
        )
    }
}

@Composable
private fun TerminalTopBar(viewmodel: TerminalViewModel, onMenuClick: () -> Unit) {
    var rotationAngle by remember { mutableStateOf(1080f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = 1500,
            delayMillis = 500
        ),
        label = "TerminalRotation"
    )
    LaunchedEffect(Unit) {
        rotationAngle -= 1080f
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .padding(0.dp, 12.dp, 0.dp, 0.dp)
                    .size(44.dp)
                    .background(Color.Transparent, shape = CircleShape)
                    .padding(0.dp, 10.dp, 0.dp, 0.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = dedeadend.dterminal.R.mipmap.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable(true) {
                            rotationAngle -= 1080f
                        }
                        .graphicsLayer {
                            rotationZ = animatedRotation
                        },
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = "DTerminal",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp)
        ) {
            IconButton(onClick = onMenuClick)
            {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tools",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            DropdownMenu(

                expanded = viewmodel.toolsMenu,
                onDismissRequest = { viewmodel.toggleToolsMenu(false) },
                offset = DpOffset(0.dp, 10.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Clear output") },
                    onClick = {
                        viewmodel.toggleToolsMenu(false)
                        viewmodel.clearOutput()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Terminate process") },
                    onClick = {
                        viewmodel.toggleToolsMenu(false)
                        viewmodel.terminate()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (viewmodel.isRoot) "Switch to Shell mode" else "Switch to Root mode") },
                    onClick = {
                        viewmodel.toggleToolsMenu(false)
                        viewmodel.toggleRoot()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }

    }
}

/*
@Preview(showBackground = true)
@Composable
fun TerminalPreview() {
    DTerminalTheme {
        Terminal()
    }
}
*/