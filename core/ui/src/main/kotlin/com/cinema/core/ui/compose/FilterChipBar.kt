package com.cinema.core.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterChipBar(
    label: String,
    resultCount: Int,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = true,
        onClick = onClear,
        label = { Text("$label ($resultCount)") },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear filter"
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier.padding(horizontal = 16.dp)
    )
}
