package com.elvettorato.routine.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elvettorato.routine.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayPicker(
    selectedDays: List<Int>,
    onDaysChanged: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf(
        1 to stringResource(R.string.mon),
        2 to stringResource(R.string.tue),
        3 to stringResource(R.string.wed),
        4 to stringResource(R.string.thu),
        5 to stringResource(R.string.fri),
        6 to stringResource(R.string.sat),
        7 to stringResource(R.string.sun)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.repeat_on),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dayLabels.forEach { (day, label) ->
                FilterChip(
                    selected = day in selectedDays,
                    onClick = {
                        val newDays = if (day in selectedDays) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                        onDaysChanged(newDays.sorted())
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
