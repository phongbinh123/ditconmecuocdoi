package com.example.ffridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ffridge.data.model.Ingredient
import com.example.ffridge.util.Constants
import com.example.ffridge.util.DateUtils
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientCard(
    ingredient: Ingredient,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image/Icon - Show photo if available, otherwise category icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (ingredient.imageUrl != null && ingredient.imageUrl.isNotEmpty()) {
                    // Show photo
                    AsyncImage(
                        model = ingredient.imageUrl,
                        contentDescription = ingredient.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        error = @androidx.compose.runtime.Composable {
                            // Fallback to category icon if image fails to load
                            Text(
                                text = Constants.CATEGORY_ICONS[ingredient.category] ?: "📦",
                                fontSize = 32.sp
                            )
                        } as Painter?
                    )
                } else {
                    // Show category icon as placeholder
                    Text(
                        text = Constants.CATEGORY_ICONS[ingredient.category] ?: "📦",
                        fontSize = 32.sp
                    )
                }
            }

            // Ingredient info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Quantity
                Text(
                    text = "${ingredient.quantity} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category label
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = ingredient.category.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Expiry info with days remaining
                ingredient.expiryDate?.let { expiryDate ->
                    val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(
                        expiryDate - System.currentTimeMillis()
                    ).toInt()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(DateUtils.getExpiryStatusColor(expiryDate)))
                        )

                        Text(
                            text = when {
                                daysUntilExpiry < 0 -> "Expired ${-daysUntilExpiry} days ago"
                                daysUntilExpiry == 0 -> "Expires today"
                                daysUntilExpiry == 1 -> "Expires tomorrow"
                                daysUntilExpiry <= 7 -> "Expires in $daysUntilExpiry days"
                                else -> DateUtils.getExpiryStatusText(expiryDate)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(DateUtils.getExpiryStatusColor(expiryDate)),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Calories if available
                ingredient.calories?.let { calories ->
                    if (calories > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "$calories kcal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action buttons column
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Edit button
                onEdit?.let { editAction ->
                    IconButton(
                        onClick = editAction,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete Ingredient?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will permanently delete \"${ingredient.name}\". This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
