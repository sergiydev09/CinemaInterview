package com.cinema.core.ai.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cinema.core.ai.domain.manager.AIMode

@Composable
fun AIFab(
    aiMode: AIMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = aiMode != AIMode.INACTIVE

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.Close else Icons.Default.AutoAwesome,
            contentDescription = if (isActive) "Deactivate AI" else "Activate AI"
        )
    }
}
