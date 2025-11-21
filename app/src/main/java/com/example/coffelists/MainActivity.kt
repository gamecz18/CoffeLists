package com.example.coffelists

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.coffelists.ui.theme.CoffeListsTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoffeListsTheme {
                CoffeeAppUI()
            }
        }
    }
}


@Composable
fun searchDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, RoastLevel? ) -> Unit,
    onValueChange: (String) -> Unit

)
{

    var name by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedRoast by remember { mutableStateOf<RoastLevel?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vyhledat kávu") },
        text =
            {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            onValueChange(it)
                        },
                        label = { Text("Název kávy") }

                    )


                    Box() {
                        OutlinedTextField(
                            value = selectedRoast?.czJmeno ?: "Libovolné pražení",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {

                                IconButton({ expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            })


                        DropdownMenu(

                            expanded = expanded,
                            onDismissRequest = { expanded = false }

                        ) {
                            DropdownMenuItem(
                                text = { Text("Libovolné pražení") },
                                onClick = {
                                    selectedRoast = null
                                    expanded = false
                                }
                            )

                            HorizontalDivider()
                            RoastLevel.entries.forEach { roastLevel ->
                                DropdownMenuItem(
                                    text = { Text(roastLevel.czJmeno) },
                                    onClick = {
                                        selectedRoast = roastLevel
                                        expanded = false
                                    }

                                )

                            }


                        }

                    }
                    Text(
                        text = "Můžeš filtrovat podle názvu nebo typu pražení. Pokud necháš některé pole prázdné, bude ignorováno. (Pro výchozí hledání nech obě pole prázdná.)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            },


        confirmButton = {
            Button(onClick = { onConfirm(name, selectedRoast) }) {
                Text("Hledat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušit")
            }
        }
    )


}


@Composable
fun CoffeeAppUI() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { CoffeeFileWork(context) }
    val scope = rememberCoroutineScope()

    var coffees by remember { mutableStateOf(listOf<Coffee>()) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        coffees = repository.getAllCoffees()
        isLoading = false
    }

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            CoffeeListScreen(
                coffees = coffees,
                isLoading = isLoading,
                onAddClick = {
                    navController.navigate("addCoffee")
                },
                onCoffeeClick = { coffee ->
                    navController.navigate("coffeeDetail/${coffee.id}")
                }
            )
        }

        composable("coffeeDetail/{coffeeId}") { backStackEntry ->
            val coffeeId = backStackEntry.arguments?.getString("coffeeId")
            val coffee = coffees.find { it.id == coffeeId }

            if (coffee != null) {
                CoffeeInfoView(
                    coffee = coffee,
                    onSave = { updatedCoffee ->
                        scope.launch {
                            repository.updateCoffee(updatedCoffee)
                            coffees = repository.getAllCoffees()
                        }
                    },
                    onDelete = {
                        scope.launch {
                            repository.deleteCoffee(coffee.id)
                            coffees = repository.getAllCoffees()
                            navController.popBackStack()
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }


        composable("addCoffee") {
            AddCoffeeScreen(
                existingCoffee = null,
                onSaveCoffee = { newCoffee ->
                    scope.launch {
                        repository.addCoffee(newCoffee)
                        coffees = repository.getAllCoffees()
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeListScreen(
    coffees: List<Coffee>,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onCoffeeClick: (Coffee) -> Unit
)
{
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var filterDialog by remember { mutableStateOf(false) }
    var filtredCoffes by remember { mutableStateOf(listOf<Coffee>()) }
    var isFiltered by rememberSaveable { mutableStateOf(false) }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Coffee List") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {



                },
                floatingActionButton =
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth().absolutePadding(left = 16.dp, right = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                        {
                            FloatingActionButton(
                                onClick = { filterDialog = true },
                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                Icon(Icons.Filled.Search, contentDescription = "Vyhledat kávů")
                            }

                            FloatingActionButton(
                                onClick = onAddClick,
                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Přidat kávu")
                            }

                        }


                }

            )

        }


    ) { paddingValues ->

        if (filterDialog) {
            searchDialog(
                onDismiss = { filterDialog = false },
                onConfirm = { name, roast ->

                    if (name.isBlank() && roast == null) {
                        isFiltered = false
                        filtredCoffes = coffees
                        filterDialog = false
                        return@searchDialog
                    }

                        filtredCoffes = coffees.filter { coffee ->

                        val nameMatch = coffee.name.contains(name, ignoreCase = true)
                        val roastMatch = roast == null || coffee.roastLevel == roast

                        nameMatch && roastMatch

                    }

                    isFiltered = true
                    filterDialog= false

                },
                onValueChange = { _ ->

                }
            )
        }

        val listToShow = if (isFiltered) filtredCoffes else coffees

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {




                if (listToShow.isEmpty()) {
                    if ( isFiltered )
                    {
                        item { Text(
                            text = "Nebyly nalezeny žádné kávy odpovídající filtru.",
                            modifier = Modifier.padding(16.dp)
                        )  }

                    }else {
                        item {
                            Text(
                                text = "Zatím žádné kávy. Klikni na ➕",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }


                }

                items(listToShow.size) { index ->
                    val coffee = listToShow[index]

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onCoffeeClick(coffee)
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    {
                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Fotka kávy
                            if (coffee.imagePath != null && coffee.imagePath != "null") {
                                AsyncImage(
                                    model = coffee.imagePath,
                                    contentDescription = "Fotka kávy",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }

                            // Informace o kávě
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // Název kávy
                                Text(
                                    text = coffee.name,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                // Poznámky
                                Text(
                                    text = coffee.notes.ifBlank { "Bez poznámky" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (coffee.notes.isBlank()) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Úroveň pražení
                                Text(
                                    text = "Pražení: ${coffee.roastLevel?.czJmeno ?: "Neuvedeno"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Jemnost mletí
                                Text(
                                    text = "Jemnost: ${coffee.grindLevel?.toString() ?: "Neuvedeno"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Váha in
                                Text(
                                    text = "Váha in: ${coffee.weightInGrams?.toString()?.plus("g") ?: "Neuvedeno"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Váha out
                                Text(
                                    text = "Váha out: ${coffee.weighOut?.toString()?.plus("g") ?: "Neuvedeno"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Ratio
                                val ratioText = if (coffee.weightInGrams != null && coffee.weighOut != null
                                    && coffee.weightInGrams!! > 0 && coffee.weighOut!! > 0) {
                                    val ratio = coffee.weighOut!! / coffee.weightInGrams!!
                                    "1:${"%.2f".format(ratio)}"
                                } else {
                                    "Neuvedeno"
                                }

                                Text(
                                    text = "Ratio: $ratioText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (ratioText != "Neuvedeno") {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
        }


        }
    }
}