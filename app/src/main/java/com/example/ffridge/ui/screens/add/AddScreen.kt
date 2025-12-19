package com.example.ffridge.ui.screens.add

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ffridge.data.model.IngredientCategory
import com.example.ffridge.util.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit,
    ingredientId: String? = null,
    modifier: Modifier = Modifier,
    viewModel: AddViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Success animation
    var showSuccessAnimation by remember { mutableStateOf(false) }

    // Camera photo URI
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // FIXED ORDER: Declare launchers first, then use them in permission launchers

    // Image picker launchers - DECLARE THESE FIRST
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateImage(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.updateImage(tempPhotoUri)
        }
    }

    // Permission launchers - NOW THESE CAN USE THE ABOVE LAUNCHERS
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create temp file for camera
            val photoFile = createImageFile(context)
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        }
    }

    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            showSuccessAnimation = true
            kotlinx.coroutines.delay(1500)
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isEditMode) "Edit Ingredient" else "Add Ingredient",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isEditMode) "Update details" else "Add to your fridge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Save button bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveIngredient(onSaveSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        enabled = !uiState.isSaving,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (uiState.isEditMode) Icons.Default.Update else Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isEditMode) "Update Ingredient" else "Save Ingredient",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Photo Section
                FormSection(title = "Photo (Optional)") {
                    Surface(
                        onClick = { showImageSourceDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (uiState.imageUri != null) {
                                AsyncImage(
                                    model = uiState.imageUri,
                                    contentDescription = "Ingredient photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                // Clear image button
                                IconButton(
                                    onClick = { viewModel.updateImage(null) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Add Photo",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Tap to select from gallery or camera",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Name field with icon
                FormSection(title = "Basic Information") {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Ingredient Name *") },
                        leadingIcon = {
                            Icon(Icons.Default.Restaurant, "Name")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.error?.contains("name") == true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Quantity and Unit
                FormSection(title = "Quantity") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.quantity,
                            onValueChange = { viewModel.updateQuantity(it) },
                            label = { Text("Amount *") },
                            leadingIcon = {
                                Icon(Icons.Default.Scale, "Quantity")
                            },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            isError = uiState.error?.contains("quantity") == true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Unit selector
                        Surface(
                            onClick = { showUnitPicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.unit,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select unit"
                                )
                            }
                        }
                    }
                }

                // Calories field
                FormSection(title = "Nutrition") {
                    OutlinedTextField(
                        value = uiState.calories,
                        onValueChange = { viewModel.updateCalories(it) },
                        label = { Text("Calories (Optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.LocalFireDepartment, "Calories")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("e.g., 150") },
                        suffix = { Text("kcal") }
                    )
                }

                // Category selector
                FormSection(title = "Category") {
                    CategorySelector(
                        selectedCategory = uiState.category,
                        onCategorySelected = { viewModel.updateCategory(it) }
                    )
                }

                // Expiry date
                FormSection(title = "Expiry Date (Optional)") {
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (uiState.expiryDate != null) {
                                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                            .format(Date(uiState.expiryDate!!))
                                    } else {
                                        "Select expiry date"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (uiState.expiryDate != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            if (uiState.expiryDate != null) {
                                IconButton(
                                    onClick = { viewModel.updateExpiryDate(null) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear date"
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes
                FormSection(title = "Notes (Optional)") {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Additional information") },
                        leadingIcon = {
                            Icon(Icons.Default.StickyNote2, "Notes")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        placeholder = {
                            Text(
                                text = "Storage tips, recipes, etc...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    )
                }

                // Error message
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.error ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Success animation overlay
            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = if (uiState.isEditMode) "Updated!" else "Saved!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Image source dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = {
                Text(
                    text = "Add Photo",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = {
                            showImageSourceDialog = false
                            // Check permission for gallery
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                }
                            } else {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Choose from Gallery",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            showImageSourceDialog = false
                            // Check camera permission
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                // Create temp file
                                val photoFile = createImageFile(context)
                                tempPhotoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                tempPhotoUri?.let { cameraLauncher.launch(it) }
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Take Photo",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        EnhancedDatePickerDialog(
            onDateSelected = { millis ->
                viewModel.updateExpiryDate(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Unit picker dialog
    if (showUnitPicker) {
        UnitPickerDialog(
            selectedUnit = uiState.unit,
            onUnitSelected = { unit ->
                viewModel.updateUnit(unit)
                showUnitPicker = false
            },
            onDismiss = { showUnitPicker = false }
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 20.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        content()
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val categories = IngredientCategory.values().toList()
        categories.chunked(3).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCategories.forEach { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category.name,
                        onSelected = { onCategorySelected(category.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowCategories.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: IngredientCategory,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelected,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = Constants.CATEGORY_ICONS[category.name] ?: "📦",
                fontSize = 32.sp
            )
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedDatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Select Expiry Date",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}

@Composable
private fun UnitPickerDialog(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Unit",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Constants.COMMON_UNITS.forEach { unit ->
                    Surface(
                        onClick = { onUnitSelected(unit) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (unit == selectedUnit) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = unit,
                                fontWeight = if (unit == selectedUnit) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                            if (unit == selectedUnit) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(24.dp)
    )
}

// Helper function to create temp file for camera
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "INGREDIENT_${timeStamp}_",
        ".jpg",
        storageDir
    )
}
