package dedeadend.dterminal.ui.script

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dedeadend.dterminal.domain.Script
import dedeadend.dterminal.domain.UiEvent
import dedeadend.dterminal.ui.BaseTopBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Script(
    viewModel: ScriptViewModel = hiltViewModel(),
    onSciptItemExecuteClick: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scripts by viewModel.scripts.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    var snackbarJob: Job? = null
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is UiEvent.ShowSnackbar) {
                snackbarJob?.cancel()
                snackbarJob = snackbarScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDeleteScript()
                    }
                }
            }
        }
    }

    var showIsEmptyIcon by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        showIsEmptyIcon = true
    }

    var previousIndex by remember { mutableIntStateOf(0) }
    val isScrollingUp by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex == 0 || previousIndex > scrollState.firstVisibleItemIndex
                .also {
                    previousIndex = scrollState.firstVisibleItemIndex
                }
        }
    }

    Scaffold(
//        contentWindowInsets = WindowInsets(0),
        topBar = { ScriptTopBar() },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isScrollingUp,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.addNewScript() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Script"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center

    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = scripts.isEmpty() && showIsEmptyIcon,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 1500)
                ) + scaleIn(
                    animationSpec = tween(durationMillis = 1500),
                    initialScale = 0.5f
                ),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "NoScriptIcon",
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "\nUse + button to add new script",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    8.dp,
                    paddingValues.calculateTopPadding(),
                    8.dp,
                    0.dp
                ),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = scripts, key = { item -> item.id }) { item ->
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
                    ScriptItem(
                        script = item,
                        onExecuteClick = { onSciptItemExecuteClick(item.command) },
                        onEditClick = { viewModel.startEdit(item) },
                        onDeleteSwipe = { viewModel.deleteScript(item) }
                    )
                }
            }
        }
        if (viewModel.isEditing) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.cancelEdit() },
                sheetState = sheetState
            ) {
                EditSheetContent(viewModel)
            }
        }
    }
}

@Composable
private fun ScriptItem(
    script: Script,
    onExecuteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteSwipe: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(script) {
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
                    .padding(1.dp, 9.dp)
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
                    .padding(0.dp, 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Name:",
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.labelMedium,
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
                                text = script.name,
                                textAlign = TextAlign.Left,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(enabled = true) { onEditClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(enabled = true) { onExecuteClick() },
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
    )
}

@Composable
private fun ScriptTopBar() {
    BaseTopBar(title = "Scripts", animateOnLaunch = false)
}

@Composable
fun EditSheetContent(viewModel: ScriptViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text("Script Options", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = viewModel.editingScriptName,
            onValueChange = { viewModel.onEditingScriptNameChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            label = { Text("Name") },
            isError = viewModel.editingScriptNameError.isNotEmpty(),
            supportingText = {
                Text(text = viewModel.editingScriptNameError)
            }
        )

        OutlinedTextField(
            value = viewModel.editingScriptCommand,
            onValueChange = { viewModel.onEditingScriptCommandChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            label = { Text("Command") },
            isError = viewModel.editingScriptCommandError.isNotEmpty(),
            supportingText = {
                Text(text = viewModel.editingScriptCommandError)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { viewModel.cancelEdit() }) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { viewModel.saveEdit() }) { Text("Save") }
        }
    }
}