package cz.g18.coffeelists

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun searchDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, RoastLevel? ) -> Unit,
    onValueChange: (String) -> Unit,
    initialName: String = "",
    initialRoast: RoastLevel? = null

)
{

    var name by rememberSaveable { mutableStateOf(initialName) }
    var expanded by remember { mutableStateOf(false) }
    var selectedRoast by rememberSaveable { mutableStateOf(initialRoast) }

    val anyRoastText = stringResource(R.string.any_roast)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.search_coffee_title)) },
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
                        label = { Text(stringResource(R.string.coffee_name_label)) }

                    )


                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedRoast?.let { stringResource(it.displayNameRes) } ?: anyRoastText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.roast_type_label)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(anyRoastText) },
                                onClick = {
                                    selectedRoast = null
                                    expanded = false
                                }
                            )

                            HorizontalDivider()

                            RoastLevel.entries.forEach { roastLevel ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(roastLevel.displayNameRes)) },
                                    onClick = {
                                        selectedRoast = roastLevel
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.search_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            },


        confirmButton = {
            Button(onClick = { onConfirm(name, selectedRoast) }) {
                Text(stringResource(R.string.search))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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

    var allCoffees by remember { mutableStateOf(listOf<Coffee>()) }
    var isLoading by remember { mutableStateOf(true) }


    var filterName by rememberSaveable { mutableStateOf("") }
    var filterRoast by rememberSaveable { mutableStateOf<RoastLevel?>(null) }


    val filteredCoffees = remember(allCoffees, filterName, filterRoast) {
        if (filterName.isBlank() && filterRoast == null) {
            allCoffees
        } else {
            allCoffees.filter { coffee ->
                val nameMatch = coffee.name.contains(filterName, ignoreCase = true)
                val roastMatch = filterRoast == null || coffee.roastLevel == filterRoast
                nameMatch && roastMatch
            }
        }
    }

    LaunchedEffect(Unit) {
        allCoffees = repository.getAllCoffees().reversed()
        kotlinx.coroutines.yield()
        isLoading = false
    }

    // Animation duration for smooth transitions
    val animationDuration = 400

    NavHost(
        navController = navController,
        startDestination = "home",
        // Default enter transition: slide in from right with fade
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(animationDuration))
        },
        // Default exit transition: slide out to left with fade
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(animationDuration / 2))
        },
        // Pop enter transition: slide in from left with fade (going back)
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(animationDuration))
        },
        // Pop exit transition: slide out to right with fade (going back)
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(animationDuration / 2))
        }
    ) {

        composable(
            route = "home",
            // Home screen uses fade + scale for a more subtle effect
            enterTransition = {
                fadeIn(animationSpec = tween(animationDuration)) +
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(animationDuration / 2)) +
                scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(animationDuration / 2, easing = EaseIn)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(animationDuration)) +
                scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                )
            }
        ) {
            CoffeeListScreen(
                coffees = filteredCoffees,
                isLoading = isLoading,
                currentFilterName = filterName,
                currentFilterRoast = filterRoast,
                onFilterChange = { name, roast ->
                    filterName = name
                    filterRoast = roast
                },
                onAddClick = {
                    navController.navigate("addCoffee")
                },
                onCoffeeClick = { coffee ->
                    navController.navigate("coffeeDetail/${coffee.id}")
                }
            )
        }

        composable(
            route = "coffeeDetail/{coffeeId}",
            // Detail screen slides in from right
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(animationDuration))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(animationDuration, easing = EaseIn)
                ) + fadeOut(animationSpec = tween(animationDuration / 2))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(animationDuration, easing = EaseOut)
                ) + fadeOut(animationSpec = tween(animationDuration / 2))
            }
        ) { backStackEntry ->
            val coffeeId = backStackEntry.arguments?.getString("coffeeId")
            val coffee = allCoffees.find { it.id == coffeeId }

            if (coffee != null) {
                CoffeeInfoView(
                    coffee = coffee,
                    onSave = { updatedCoffee ->
                        scope.launch {
                            repository.updateCoffee(updatedCoffee)
                            kotlinx.coroutines.yield()
                            allCoffees = repository.getAllCoffees().reversed()
                        }
                    },
                    onDelete = {
                        scope.launch {
                            navController.popBackStack()
                            repository.deleteCoffee(coffee.id)
                            kotlinx.coroutines.yield()
                            allCoffees = repository.getAllCoffees().reversed()

                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }


        composable(
            route = "addCoffee",
            // Add screen slides up from bottom for a modal-like feel
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(animationDuration))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(animationDuration, easing = EaseIn)
                ) + fadeOut(animationSpec = tween(animationDuration / 2))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(animationDuration, easing = EaseOut)
                ) + fadeOut(animationSpec = tween(animationDuration / 2))
            }
        ) {
            AddCoffeeScreen(
                existingCoffee = null,
                onSaveCoffee = { newCoffee ->
                    scope.launch {
                        repository.addCoffee(newCoffee)
                        allCoffees = repository.getAllCoffees().reversed()
                        kotlinx.coroutines.yield()
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
    currentFilterName: String,
    currentFilterRoast: RoastLevel?,
    onFilterChange: (String, RoastLevel?) -> Unit,
    onAddClick: () -> Unit,
    onCoffeeClick: (Coffee) -> Unit
)
{
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var filterDialog by remember { mutableStateOf(false) }

    val isFiltered = currentFilterName.isNotBlank() || currentFilterRoast != null

    val notSpecifiedText = stringResource(R.string.not_specified)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.coffee_list_title)) },
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
                                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_coffee))
                            }

                            FloatingActionButton(
                                onClick = onAddClick,
                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_coffee))
                            }

                        }


                }

            )

        }


    ) { paddingValues ->

        if (filterDialog) {
            searchDialog(
                onDismiss = { filterDialog = false },
                initialName = currentFilterName,
                initialRoast = currentFilterRoast,
                onConfirm = { name, roast ->
                    onFilterChange(name, roast)
                    filterDialog = false
                },
                onValueChange = { _ -> }
            )
        }

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
                if (coffees.isEmpty()) {
                    if (isFiltered) {
                        item {
                            Text(
                                text = stringResource(R.string.no_coffees_filter),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.no_coffees_yet),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                items(coffees.size) { index ->
                    val coffee = coffees[index]

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
                            if (coffee.imagePath != null) {
                                val imageUri = ImageHelper.getImageUri(coffee.imagePath)
                                if (imageUri != null) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = stringResource(R.string.coffee_photo),
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
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
                                    text = coffee.notes.ifBlank { stringResource(R.string.no_notes) },
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
                                val roastText = coffee.roastLevel?.let { stringResource(it.displayNameRes) } ?: notSpecifiedText
                                Text(
                                    text = stringResource(R.string.roast_format, roastText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Jemnost mletí
                                Text(
                                    text = stringResource(R.string.grind_format, coffee.grindLevel?.toString() ?: notSpecifiedText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Váha in
                                Text(
                                    text = stringResource(R.string.weight_in_format, coffee.weightInGrams?.let { "${it}g" } ?: notSpecifiedText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Váha out
                                Text(
                                    text = stringResource(R.string.weight_out_format, coffee.weighOut?.let { "${it}g" } ?: notSpecifiedText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Ratio
                                val ratioText = if (coffee.weightInGrams != null && coffee.weighOut != null
                                    && coffee.weightInGrams!! > 0 && coffee.weighOut!! > 0) {
                                    val ratio = coffee.weighOut!! / coffee.weightInGrams!!
                                    "1:${"%.2f".format(ratio)}"
                                } else {
                                    notSpecifiedText
                                }

                                Text(
                                    text = stringResource(R.string.ratio_format, ratioText),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (ratioText != notSpecifiedText) {
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
