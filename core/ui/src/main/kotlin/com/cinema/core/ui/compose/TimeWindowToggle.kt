package com.cinema.core.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cinema.core.domain.model.TimeWindow

@Composable
fun TimeWindowToggle(
    selectedTimeWindow: TimeWindow,
    onTimeWindowSelected: (TimeWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        FilterChip(
            selected = selectedTimeWindow == TimeWindow.DAY,
            onClick = { onTimeWindowSelected(TimeWindow.DAY) },
            label = { Text("Today") },
            modifier = Modifier.padding(end = 8.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
            )
        )
        FilterChip(
            selected = selectedTimeWindow == TimeWindow.WEEK,
            onClick = { onTimeWindowSelected(TimeWindow.WEEK) },
            label = { Text("This Week") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
            )
        )
    }
}
