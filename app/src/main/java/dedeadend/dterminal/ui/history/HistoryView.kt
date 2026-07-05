package dedeadend.dterminal.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dedeadend.dterminal.core.BaseTopBar
import dedeadend.dterminal.domain.model.History
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun History(
    viewModel: HistoryViewModel = hiltViewModel(),
    onHistoryItemExecuteClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    var snackbarJob: Job? = null

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HistoryUiEffect.ShowSnackbar -> {
                    snackbarJob?.cancel()
                    snackbarJob = snackbarScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = effect.actionLabel,
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onEvent(HistoryUiEvent.UndoDeleteHistoryItems)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { HistoryTopBar(onClearHistoryClick = { viewModel.onEvent(HistoryUiEvent.ClearHistory) }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = uiState.showEmptyState,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 1000, delayMillis = 500),
                    initialAlpha = 0.0f
                ) + scaleIn(
                    animationSpec = tween(durationMillis = 1000, delayMillis = 500),
                    initialScale = 0.5f
                ),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "NoHistoryIcon",
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        "No History Yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            }
        }
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            reverseLayout = true
        ) {
            items(items = uiState.history, key = { item -> item.id }) { item ->
                val animatedProgress = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    animatedProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 500)
                    )
                }
                Box(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .graphicsLayer {
                            alpha = animatedProgress.value
                            translationY = (1f - animatedProgress.value) * 50f
                            scaleX = 0.5f + (animatedProgress.value * 0.5f)
                        }
                ) {
                    HistoryItem(
                        history = item,
                        onExecuteClick = { onHistoryItemExecuteClick(item.command) },
                        onDeleteSwipe = { viewModel.onEvent(HistoryUiEvent.DeleteHistoryItem(item)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    history: History,
    onExecuteClick: () -> Unit,
    onDeleteSwipe: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(history) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        onDismiss = { dismissState ->
            if (dismissState == SwipeToDismissBoxValue.StartToEnd || dismissState == SwipeToDismissBoxValue.EndToStart) {
                onDeleteSwipe()
            }
        },
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(9.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red),
                horizontalArrangement =
                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                        Arrangement.Start
                    else
                        Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(16.dp, 0.dp)
                )
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Command:",
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic
                    )
                    Row(
                        modifier = Modifier
                            .height(intrinsicSize = IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(4.dp),
                            thickness = 2.dp,
                            color = Color.Gray
                        )
                        SelectionContainer(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = history.command,
                                textAlign = TextAlign.Left,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable(enabled = true) {
                                        onExecuteClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
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
    )
}

@Composable
private fun HistoryTopBar(onClearHistoryClick: () -> Unit) {
    BaseTopBar(title = "History", animateOnLaunch = false, actions = {
        Box(modifier = Modifier.padding(0.dp, 24.dp, 0.dp, 0.dp)) {
            IconButton(onClick = onClearHistoryClick)
            {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear History",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    })
}