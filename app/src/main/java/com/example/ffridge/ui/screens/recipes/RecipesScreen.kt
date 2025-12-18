@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ffridge.ui.screens.recipes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ffridge.ui.components.EnhancedRecipeCard
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ffridge.data.model.Recipe
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRecipeDetails by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        floatingActionButton = {
            AnimatedFloatingActionButton(
                isGenerating = uiState.isGenerating,
                hasIngredients = uiState.availableIngredients.isNotEmpty(),
                onClick = { viewModel.generateRecipe() }
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
                // Header with stats
                RecipesHeader(
                    recipeCount = uiState.recipes.size,
                    availableIngredientsCount = uiState.availableIngredients.size,
                    modifier = Modifier.padding(16.dp)
                )

                // Filter chips
                RecipeFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.selectFilter(it) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Content
                when {
                    uiState.isLoading -> {
                        LoadingRecipesState()
                    }
                    uiState.isGenerating -> {
                        GeneratingRecipeState()
                    }
                    uiState.filteredRecipes.isEmpty() -> {
                        EmptyRecipesState(
                            hasIngredients = uiState.availableIngredients.isNotEmpty(),
                            onGenerateClick = { viewModel.generateRecipe() }
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.filteredRecipes,
                                key = { it.id }
                            ) { recipe ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut() + slideOutVertically()
                                ) {
                                    EnhancedRecipeCard(
                                        recipe = recipe,
                                        onClick = { showRecipeDetails = recipe },
                                        onFavoriteClick = { viewModel.toggleFavorite(recipe.id) },
                                        onDeleteClick = { viewModel.deleteRecipe(recipe) }
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }

            // Error snackbar
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }

    // Recipe details bottom sheet
    showRecipeDetails?.let { recipe ->
        RecipeDetailsBottomSheet(
            recipe = recipe,
            onDismiss = { showRecipeDetails = null },
            onCookClick = { /* TODO: Start cooking mode */ }
        )
    }
}

@Composable
private fun RecipesHeader(
    recipeCount: Int,
    availableIngredientsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = recipeCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Recipes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Kitchen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = availableIngredientsCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeFilterRow(
    selectedFilter: RecipeFilter,
    onFilterSelected: (RecipeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(RecipeFilter.entries) { filter ->
            val isSelected = selectedFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (filter) {
                                RecipeFilter.All -> Icons.Default.GridView
                                RecipeFilter.Favorites -> Icons.Default.Favorite
                                RecipeFilter.Quick -> Icons.Default.Timer
                                RecipeFilter.Easy -> Icons.Default.ThumbUp
                                RecipeFilter.WithAvailableIngredients -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = filter.name.replace("_", " "),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun AnimatedFloatingActionButton(
    isGenerating: Boolean,
    hasIngredients: Boolean,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Generate"
                )
            }
        },
        text = {
            Text(
                text = if (isGenerating) "Generating..." else "AI Generate",
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = if (hasIngredients) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (hasIngredients) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .then(if (isGenerating) Modifier else Modifier)
    )
}

@Composable
private fun GeneratingRecipeState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated cooking icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👨‍🍳",
                    fontSize = 60.sp
                )
            }

            Text(
                text = "Cooking up something special...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "AI is analyzing your ingredients",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            LinearProgressIndicator(
                modifier = Modifier.width(200.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingRecipesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyRecipesState(
    hasIngredients: Boolean,
    onGenerateClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🍳",
                fontSize = 80.sp
            )

            Text(
                text = if (hasIngredients) {
                    "No recipes yet"
                } else {
                    "Add ingredients first"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (hasIngredients) {
                    "Let AI create amazing recipes from your ingredients"
                } else {
                    "Add ingredients to your fridge to generate recipes"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (hasIngredients) {
                Button(
                    onClick = onGenerateClick,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Recipe",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Continue with RecipeDetailsBottomSheet in next part...
