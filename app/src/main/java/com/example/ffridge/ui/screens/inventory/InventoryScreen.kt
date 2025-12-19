package com.example.ffridge.ui.screens.inventory

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ffridge.data.model.IngredientCategory
import com.example.ffridge.ui.components.IngredientCard
import com.example.ffridge.ui.components.NotificationToast
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
    viewModel: InventoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNotification by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.expiringCount) {
        if (uiState.expiringCount > 0) {
            showNotification = true
            delay(5000)
            showNotification = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ffridge.",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.displayLarge.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add Item") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Stats Header
                StatsHeader(
                    totalCount = uiState.ingredients.size,
                    expiringCount = uiState.expiringCount,
                    expiredCount = uiState.expiredCount,
                    modifier = Modifier.padding(16.dp)
                )

                // Category filter
                CategoryFilterRow(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Search + Sort
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.searchIngredients(it) },
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = { showSortDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (uiState.sortOption) {
                                    SortOption.NAME_ASC -> "A-Z"
                                    SortOption.NAME_DESC -> "Z-A"
                                    SortOption.DATE_ADDED_ASC -> "Old"
                                    SortOption.DATE_ADDED_DESC -> "New"
                                    SortOption.EXPIRY_ASC -> "Soon"
                                    SortOption.EXPIRY_DESC -> "Later"
                                    SortOption.QUANTITY_ASC -> "Low"
                                    SortOption.QUANTITY_DESC -> "High"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // List
                when {
                    uiState.isLoading -> LoadingState()
                    uiState.filteredIngredients.isEmpty() -> EmptyState(uiState.searchQuery)
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.filteredIngredients,
                                key = { it.id }
                            ) { ingredient ->
                                IngredientCard(
                                    ingredient = ingredient,
                                    onClick = {  },
                                    onDelete = { viewModel.deleteIngredient(ingredient) },
                                    onEdit = { onEditClick(ingredient.id) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }

            // Notification
            AnimatedVisibility(
                visible = showNotification,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                NotificationToast(
                    message = "${uiState.expiringCount} items expiring soon!",
                    isVisible = showNotification,
                    onDismiss = { showNotification = false }
                )
            }
        }
    }

    if (showSortDialog) {
        SortDialog(
            currentSort = uiState.sortOption,
            onSortSelected = {
                viewModel.setSortOption(it)
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
private fun SortDialog(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Sort, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Sort By", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SortOption.entries.forEach { option ->
                    Surface(
                        onClick = { onSortSelected(option) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (option == currentSort) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.displayName,
                                fontWeight = if (option == currentSort) FontWeight.Bold else FontWeight.Normal
                            )
                            if (option == currentSort) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun StatsHeader(
    totalCount: Int,
    expiringCount: Int,
    expiredCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            count = totalCount,
            label = "Total",
            icon = Icons.Default.Inventory,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        if (expiringCount > 0) {
            StatCard(
                count = expiringCount,
                label = "Expiring",
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        if (expiredCount > 0) {
            StatCard(
                count = expiredCount,
                label = "Expired",
                icon = Icons.Default.ErrorOutline,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(32.dp))
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All") + IngredientCategory.entries.map { it.name }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (category != "All") {
                            Text(
                                text = com.example.ffridge.util.Constants.CATEGORY_ICONS[category] ?: "",
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = category.lowercase().replaceFirstChar { it.uppercase() },
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search...") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(Modifier.size(48.dp), strokeWidth = 4.dp)
            Text("Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(searchQuery: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = if (searchQuery.isEmpty()) "🍽️" else "🔍", fontSize = 80.sp)
            Text(
                text = if (searchQuery.isEmpty()) "Your fridge is empty" else "No items found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (searchQuery.isEmpty()) "Add your first ingredient" else "Try different keywords",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
