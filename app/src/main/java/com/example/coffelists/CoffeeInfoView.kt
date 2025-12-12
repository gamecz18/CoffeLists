package cz.g18.coffeelists
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeInfoView(
    coffee: Coffee,
    onSave: (Coffee) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (isEditing) {

        AddCoffeeScreen(
            existingCoffee = coffee,
            onSaveCoffee = { updatedCoffee ->
                onSave(updatedCoffee)
                isEditing = false
            },
            onCancel = {
                isEditing = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail kávy") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Zpět")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Smazat",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fotka
                coffee.imagePath?.let { path ->
                    val imageUri = ImageHelper.getImageUri(path)
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Fotka kávy",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Název
                Text(
                    text = coffee.name,
                    style = MaterialTheme.typography.headlineLarge
                )

                // Informace
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Informace",
                            style = MaterialTheme.typography.titleMedium
                        )

                        HorizontalDivider()

                        InfoRow("Pražení", coffee.roastLevel?.czJmeno ?: "Neuvedeno")
                        InfoRow("Poznámky", coffee.notes.ifBlank { "Bez poznámky" })
                        InfoRow("Jemnost mletí", coffee.grindLevel?.toString() ?: "Neuvedeno")
                        InfoRow("Váha in", coffee.weightInGrams?.let { "${it}g" } ?: "Neuvedeno")
                        InfoRow("Váha out", coffee.weighOut?.let { "${it}g" } ?: "Neuvedeno")

                        if (coffee.weightInGrams != null && coffee.weighOut != null
                            && coffee.weightInGrams!! > 0 && coffee.weighOut!! > 0) {
                            val ratio = coffee.weighOut!! / coffee.weightInGrams!!
                            InfoRow(
                                "Ratio",
                                "1:${"%.2f".format(ratio)}",
                                valueColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Tlačítko Upravit
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upravit")
                }
            }
        }

        // Dialog pro potvrzení smazání
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Smazat kávu?") },
                text = { Text("Opravdu chcete smazat kávu \"${coffee.name}\"? Tuto akci nelze vrátit zpět.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDelete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Smazat")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Zrušit")
                    }
                }
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}