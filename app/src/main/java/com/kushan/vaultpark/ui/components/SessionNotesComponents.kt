package com.kushan.vaultpark.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kushan.vaultpark.model.SessionTag
import com.kushan.vaultpark.ui.theme.*

// ============ ✨ STEP 2: SESSION NOTES & TAGS ============

/**
 * Add Notes Dialog - Shown after parking session ends
 */
@Composable
fun AddSessionNotesDialog(
    onDismiss: () -> Unit,
    onSave: (notes: String, tags: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var notes by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<Set<SessionTag>>(emptySet()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Add Details",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextLight
                        )
                        Text(
                            text = "Add notes & categorize your visit",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextSecondaryDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryDark
                        )
                    }
                }

                // Tags Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Category",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextLight
                    )

                    // Tag Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SessionTag.values().forEach { tag ->
                            SessionTagChip(
                                tag = tag,
                                isSelected = selectedTags.contains(tag),
                                onClick = {
                                    selectedTags = if (selectedTags.contains(tag)) {
                                        selectedTags - tag
                                    } else {
                                        selectedTags + tag
                                    }
                                }
                            )
                        }
                    }
                }

                // Notes Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Notes (Optional)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextLight
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { if (it.length <= 200) notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = {
                            Text(
                                "e.g., Client meeting, Shopping for groceries...",
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = TextTertiaryDark
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 4,
                        supportingText = {
                            Text(
                                text = "${notes.length}/200",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = TextTertiaryDark,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "Skip",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSave(notes, selectedTags.map { it.name })
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Save",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Session Tag Chip
 */
@Composable
fun SessionTagChip(
    tag: SessionTag,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) BorderStroke(1.5.dp, PrimaryPurple) else null,
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag.icon,
                fontSize = 14.sp
            )
            Text(
                text = tag.displayName,
                fontFamily = Poppins,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp,
                color = if (isSelected) Color.White else TextLight
            )
        }
    }
}

/**
 * Session Tags Display - Shows tags in session card
 */
@Composable
fun SessionTagsDisplay(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(tags) { tagName ->
            val tag = SessionTag.fromString(tagName)
            SessionTagBadge(tag = tag)
        }
    }
}

@Composable
private fun SessionTagBadge(tag: SessionTag) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PrimaryPurple.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag.icon,
                fontSize = 11.sp
            )
            Text(
                text = tag.displayName,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = PrimaryPurple
            )
        }
    }
}

/**
 * Session Notes Display - Shows notes in session detail
 */
@Composable
fun SessionNotesDisplay(
    notes: String,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.StickyNote2,
                contentDescription = "Notes",
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = notes,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = TextLight,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Tag Filter Row - For filtering sessions by tag
 */
@Composable
fun TagFilterRow(
    selectedTags: Set<SessionTag>,
    onTagSelected: (SessionTag) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by Category",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextLight
            )

            if (selectedTags.isNotEmpty()) {
                TextButton(
                    onClick = onClearFilters,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Clear All",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = PrimaryPurple
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(SessionTag.values()) { tag ->
                SessionTagChip(
                    tag = tag,
                    isSelected = selectedTags.contains(tag),
                    onClick = { onTagSelected(tag) }
                )
            }
        }
    }
}

/**
 * Monthly Summary by Category Card
 */
@Composable
fun MonthlyCategorySummaryCard(
    tagDistribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    if (tagDistribution.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PieChart,
                    contentDescription = "Summary",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "This Month by Category",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextLight
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tagDistribution.entries
                    .sortedByDescending { it.value }
                    .forEach { (tagName, count) ->
                        val tag = SessionTag.fromString(tagName)
                        CategorySummaryItem(
                            tag = tag,
                            count = count,
                            totalCount = tagDistribution.values.sum()
                        )
                    }
            }
        }
    }
}

@Composable
private fun CategorySummaryItem(
    tag: SessionTag,
    count: Int,
    totalCount: Int
) {
    val percentage = if (totalCount > 0) (count.toFloat() / totalCount) * 100 else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = tag.icon,
                fontSize = 16.sp
            )
            Text(
                text = tag.displayName,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextLight
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress Bar
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percentage / 100f)
                        .background(PrimaryPurple)
                )
            }

            Text(
                text = "$count visits",
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = PrimaryPurple,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}

/**
 * FlowRow for wrapping tag chips
 */
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        
        var yPosition = 0
        var xPosition = 0
        var maxHeight = 0
        
        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        
        placeables.forEach { placeable ->
            if (xPosition + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                xPosition = 0
                yPosition += maxHeight + 8.dp.roundToPx()
                maxHeight = 0
            }
            currentRow.add(placeable)
            xPosition += placeable.width + 8.dp.roundToPx()
            maxHeight = maxOf(maxHeight, placeable.height)
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        val totalHeight = yPosition + maxHeight
        
        layout(constraints.maxWidth, totalHeight) {
            var currentY = 0
            rows.forEach { row ->
                var currentX = 0
                var rowHeight = 0
                row.forEach { placeable ->
                    placeable.placeRelative(currentX, currentY)
                    currentX += placeable.width + 8.dp.roundToPx()
                    rowHeight = maxOf(rowHeight, placeable.height)
                }
                currentY += rowHeight + 8.dp.roundToPx()
            }
        }
    }
}

@Composable
private fun Layout(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    measurePolicy: androidx.compose.ui.layout.MeasurePolicy
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier,
        measurePolicy = measurePolicy
    )
}