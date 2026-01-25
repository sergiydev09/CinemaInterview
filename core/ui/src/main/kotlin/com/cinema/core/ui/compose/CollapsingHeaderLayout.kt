package com.cinema.core.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cinema.core.ui.R

@Composable
fun CollapsingHeaderLayout(
    onBackClick: () -> Unit,
    expandedHeaderHeight: Dp = 220.dp,
    collapsedHeaderHeight: Dp = 56.dp,
    header: @Composable (progress: Float) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val headerHeightPx = with(density) { (expandedHeaderHeight - collapsedHeaderHeight).toPx() }

    val headerProgress by remember {
        derivedStateOf {
            (scrollState.value / headerHeightPx).coerceIn(0f, 1f)
        }
    }

    val currentHeaderHeight = expandedHeaderHeight - (expandedHeaderHeight - collapsedHeaderHeight) * headerProgress

    Box(modifier = modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(expandedHeaderHeight))
            content()
        }

        // Collapsing header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentHeaderHeight)
        ) {
            header(headerProgress)
        }

        // Floating toolbar with back button and actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingBackButton(onClick = onBackClick)
            actions()
        }
    }
}

@Composable
fun FloatingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            icon()
        }
    }
}
