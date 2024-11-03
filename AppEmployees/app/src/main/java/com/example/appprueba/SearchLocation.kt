package com.example.appprueba

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.snapshots.SnapshotStateList

// Main screen with navigation
@Composable
fun SearchLocationScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search_location") {
        composable("search_location") {
            SearchLocation(navController)
        }
        composable("employee_list/{locationId}/{locationName}") { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId")?.toInt() ?: 0
            val locationName = backStackEntry.arguments?.getString("locationName") ?: ""
            LocationEmployeesListScreen(
                locationId = locationId,
                locationName = locationName,
                navController = navController)
        }
    }
}

// Main Composable for searching locations
@Composable
fun SearchLocation(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    val locations = remember { mutableStateListOf<Location>() }
    var filteredLocations by remember { mutableStateOf(emptyList<Location>()) }

    // Load locations data from Firebase
    LaunchedEffect(Unit) {
        getLocationsData(locations) {
            // Sort locations alphabetically once data is loaded
            locations.sortBy { it.name }
            filteredLocations = locations
        }
    }

    // Filter locations as the user types
    LaunchedEffect(searchQuery) {
        filteredLocations = locations.filter {
            it.name.lowercase().contains(searchQuery.lowercase())
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search office by name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display filtered locations in alphabetical order
        if (filteredLocations.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredLocations) { location ->
                    Text(
                        text = location.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                // Navigate to employee list screen, passing location ID and name
                                navController.navigate("employee_list/${location.id}/${location.name}")
                            }
                    )
                }
            }
        } else {
            Text(
                text = "No results found.",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Composable for displaying a list of employees for a specific location
@Composable
fun LocationEmployeesListScreen(locationId: Int, locationName: String, navController: NavHostController) {
    var employeesInLocation by remember { mutableStateOf(emptyList<Employee>()) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var showEmployeeDetails by remember { mutableStateOf(false) }
    val continents = remember { mutableStateListOf<Continent>() }
    val countries = remember { mutableStateListOf<Country>() }
    val cities = remember { mutableStateListOf<City>() }
    val locations = remember { mutableStateListOf<Location>() }
    val departments = remember { mutableStateListOf<Department>() }

    LaunchedEffect(Unit) {
        getContinentsData(continents) {}
        getCountriesData(countries) {}
        getCitiesData(cities) {}
        getLocationsData(locations) {}
        getDepartmentsData(departments) {}
    }

    // Load employees for the selected location
    LaunchedEffect(locationId) {
        getEmployeesByLocation(locationId) { employees ->
            employeesInLocation = employees.sortedBy { it.name }  // Sort employees alphabetically by name
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { navController.popBackStack() }, // Volver a la pantalla anterior
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, // Puedes usar otro ícono si prefieres
                    contentDescription = "Comeback",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Employees in $locationName:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        if (employeesInLocation.isNotEmpty()) {
            LazyColumn {
                items(employeesInLocation) { employee ->
                    Text(
                        text = "${employee.name} (${employee.position})",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                selectedEmployee = employee
                                showEmployeeDetails = true
                            }
                    )
                }
            }
        } else {
            Text(
                text = "No results found.",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        if (showEmployeeDetails && selectedEmployee != null) {
            EmployeeDetailsDialog(
                employee = selectedEmployee!!,
                availableCountries = countries,
                availableCities = cities,
                availableContinents = continents,
                availableLocations = locations,
                availableDepartments = departments,
                onDismiss = { showEmployeeDetails = false }
            )
        }
    }
}

// Function to get location data from Firebase
fun getLocationsData(locations: SnapshotStateList<Location>, onComplete: () -> Unit) {
    val database = Firebase.database
    val locationsRef = database.getReference("locations")

    locationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            locations.clear()
            for (locationSnapshot in snapshot.children) {
                val location = locationSnapshot.getValue(Location::class.java)
                if (location != null) {
                    locations.add(location)
                }
            }
            onComplete()
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error reading database: ${error.message}")
        }
    })
}

// Function to get employees by location
private fun getEmployeesByLocation(locationId: Int, onEmployeesUpdated: (List<Employee>) -> Unit) {
    val database = Firebase.database
    val employeesRef = database.getReference("Employees")

    employeesRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val employeesInLocation = mutableListOf<Employee>()
            for (employeeSnapshot in snapshot.children) {
                val employee = employeeSnapshot.getValue(Employee::class.java)
                if (employee != null && employee.office.location == locationId) {
                    employeesInLocation.add(employee)
                }
            }
            onEmployeesUpdated(employeesInLocation)
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error reading database: ${error.message}")
        }
    })
}
