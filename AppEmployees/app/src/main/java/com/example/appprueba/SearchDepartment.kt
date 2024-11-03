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

// Main screen with navigation
// Main screen with navigation
@Composable
fun SearchDepartmentScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search_department") {
        composable("search_department") {
            SearchDepartment(navController)
        }
        composable("employee_list/{departmentId}/{departmentName}") { backStackEntry ->
            val departmentId = backStackEntry.arguments?.getString("departmentId")?.toInt() ?: 0
            val departmentName = backStackEntry.arguments?.getString("departmentName") ?: ""

            // Llamada a DepartmentEmployeesListScreen pasando navController
            DepartmentEmployeesListScreen(
                departmentId = departmentId,
                departmentName = departmentName,
                navController = navController // Aquí pasamos el navController
            )
        }
    }
}


// Main Composable for searching departments
@Composable
fun SearchDepartment(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    val departments = remember { mutableStateListOf<Department>() }
    var filteredDepartments by remember { mutableStateOf(emptyList<Department>()) }

    // Load departments data from Firebase
    LaunchedEffect(Unit) {
        getDepartmentsData(departments) {
            // Sort departments alphabetically once data is loaded
            departments.sortBy { it.name }
            filteredDepartments = departments
        }
    }

    // Filter departments as the user types
    LaunchedEffect(searchQuery) {
        filteredDepartments = departments.filter {
            it.name.lowercase().contains(searchQuery.lowercase())
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search department by name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display filtered departments in alphabetical order
        if (filteredDepartments.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredDepartments) { department ->
                    Text(
                        text = department.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                // Navigate to employee list screen, passing department ID and name
                                navController.navigate("employee_list/${department.id}/${department.name}")
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

@Composable
fun DepartmentEmployeesListScreen(
    departmentId: Int,
    departmentName: String,
    navController: NavHostController // Recibir navController para navegación
) {
    var employeesInDepartment by remember { mutableStateOf(emptyList<Employee>()) }
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

    // Load employees for the selected department
    LaunchedEffect(departmentId) {
        getEmployeesByDepartment(departmentId) { employees ->
            employeesInDepartment = employees.sortedBy { it.name }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Botón de "Volver"
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
                text = "Employees in $departmentName:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        if (employeesInDepartment.isNotEmpty()) {
            LazyColumn {
                items(employeesInDepartment) { employee ->
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


// Function to get employees by location
private fun getEmployeesByDepartment(departmentId: Int, onEmployeesUpdated: (List<Employee>) -> Unit) {
    val database = Firebase.database
    val employeesRef = database.getReference("Employees")

    employeesRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val employeesInLocation = mutableListOf<Employee>()
            for (employeeSnapshot in snapshot.children) {
                val employee = employeeSnapshot.getValue(Employee::class.java)
                if (employee != null && employee.departmentId == departmentId) {
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