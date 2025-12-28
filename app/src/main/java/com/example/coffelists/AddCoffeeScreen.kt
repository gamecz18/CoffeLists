package cz.g18.coffeelists

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

@Composable
fun NumericUpDown(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    step: Float = 1f,
    canBeNegative: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var tempValue by rememberSaveable { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )

        Button(onClick = {
            if (value > 0 && !canBeNegative) onValueChange(value - step)
            else if (canBeNegative) onValueChange(value - step)
        }) {
            Text("-")
        }

        Text(
            text = value.toString(),
            modifier = Modifier
                .width(50.dp)
                .clickable {
                    tempValue = value.toString()
                    showDialog = true
                },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        Button(onClick = {
            onValueChange(value + step)
        }) {
            Text("+")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.enter_value, label)) },
            text = {
                OutlinedTextField(
                    value = tempValue,
                    onValueChange = {
                        if (canBeNegative) {
                            if (it.isEmpty() || it.matches(Regex("^-?\\d*[.,]?\\d*$"))) {
                                tempValue = it
                            }
                        } else {
                            if (it.isEmpty() || it.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                tempValue = it
                            }
                        }
                    },
                    label = { Text(label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onValueChange(tempValue.replace(",", ".").toFloatOrNull() ?: 0f)
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCoffeeScreen(
    existingCoffee: Coffee? = null,
    onSaveCoffee: (Coffee) -> Unit,
    onCancel: () -> Unit
) {

    var coffeeName by rememberSaveable { mutableStateOf(existingCoffee?.name ?: "") }
    var coffeeNotes by rememberSaveable { mutableStateOf(existingCoffee?.notes ?: "") }
    var coffeeImagePath by rememberSaveable {
        mutableStateOf(existingCoffee?.imagePath)
    }
    var coffeeGroudSize by rememberSaveable { mutableStateOf(existingCoffee?.grindLevel ?: 0f) }
    var weightIn by rememberSaveable { mutableStateOf(existingCoffee?.weightInGrams ?: 0f) }
    var weightOut by rememberSaveable { mutableStateOf(existingCoffee?.weighOut ?: 0f) }
    var coffeeRoastLevel by remember { mutableStateOf(existingCoffee?.roastLevel) }
    var roastLevelExpanded by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val photosDir = File(context.filesDir, "photos")
    if (!photosDir.exists()) {
        photosDir.mkdirs()
    }

    val photoFile = File(photosDir, "coffee_${System.currentTimeMillis()}.jpg")
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    val cameraPermissionDeniedText = stringResource(R.string.camera_permission_denied)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val savedPath = ImageHelper.saveImageToInternalStorage(context, photoUri)
            coffeeImagePath = savedPath
            photoFile.delete()
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        } else {
            android.widget.Toast.makeText(
                context,
                cameraPermissionDeniedText,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = ImageHelper.saveImageToInternalStorage(context, it)
            coffeeImagePath = savedPath
        }
    }

    fun requestCameraAndTakePhoto() {
        when {
            context.checkSelfPermission(android.Manifest.permission.CAMERA) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(photoUri)
            }
            else -> {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingCoffee != null)
                            stringResource(R.string.edit_coffee_title)
                        else
                            stringResource(R.string.add_coffee_title)
                    )
                },
                navigationIcon = if (existingCoffee != null) {
                    {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                } else {
                    {}
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    )  { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Název kávy
            OutlinedTextField(
                value = coffeeName,
                onValueChange = { coffeeName = it },
                label = { Text(stringResource(R.string.coffee_name_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Úroveň pražení
            ExposedDropdownMenuBox(
                expanded = roastLevelExpanded,
                onExpandedChange = { roastLevelExpanded = it }
            ) {
                OutlinedTextField(
                    value = coffeeRoastLevel?.let { stringResource(it.displayNameRes) } ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.roast_level_label)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = roastLevelExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = roastLevelExpanded,
                    onDismissRequest = { roastLevelExpanded = false }
                ) {
                    RoastLevel.entries.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(stringResource(level.displayNameRes)) },
                            onClick = {
                                coffeeRoastLevel = level
                                roastLevelExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = coffeeNotes,
                onValueChange = { coffeeNotes = it },
                label = { Text(stringResource(R.string.notes_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Column {
                Text(stringResource(R.string.photo_label), style = MaterialTheme.typography.bodyLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = { requestCameraAndTakePhoto() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.take_photo))
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.gallery))
                    }
                }
            }

            coffeeImagePath?.let { path ->
                val imageUri = ImageHelper.getImageUri(path)
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = stringResource(R.string.photo_preview),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NumericUpDown(
                label = stringResource(R.string.grind_size_label),
                value = coffeeGroudSize,
                onValueChange = { coffeeGroudSize = it },
                step = 1f
            )

            NumericUpDown(
                label = stringResource(R.string.weight_in_label),
                value = weightIn,
                onValueChange = { weightIn = it },
                step = 1f,
                canBeNegative = false
            )

            NumericUpDown(
                label = stringResource(R.string.weight_out_label),
                value = weightOut,
                onValueChange = { weightOut = it },
                step = 1f,
                canBeNegative = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weightIn > 0 && weightOut > 0) {
                Text(
                    text = stringResource(R.string.ratio_value, weightOut / weightIn),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        val coffee = if (existingCoffee != null) {
                            existingCoffee.copy(
                                name = coffeeName,
                                notes = coffeeNotes,
                                imagePath = coffeeImagePath,
                                roastLevel = coffeeRoastLevel,
                                grindLevel = coffeeGroudSize,
                                weightInGrams = weightIn,
                                weighOut = weightOut
                            )
                        } else {
                            Coffee(
                                name = coffeeName,
                                notes = coffeeNotes,
                                imagePath = coffeeImagePath,
                                roastLevel = coffeeRoastLevel,
                                grindLevel = coffeeGroudSize,
                                weightInGrams = weightIn,
                                weighOut = weightOut
                            )
                        }
                        onSaveCoffee(coffee)

                    },
                    enabled = coffeeName.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (existingCoffee != null)
                            stringResource(R.string.save)
                        else
                            stringResource(R.string.add)
                    )
                }
            }
        }
    }
}
