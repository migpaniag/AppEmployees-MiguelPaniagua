package com.example.appprueba

// Importaciones necesarias
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.snapshots.SnapshotStateList

// Clases de datos para continentes, países y ciudades
data class Continent(val id: Int = 0, val name: String = "")
data class Country(val id: Int = 0, val name: String = "")
data class City(val id: Int = 0, val name: String = "")

data class Office(
    val location: Int = 0,
    val cityId: Int = 0,
    val countryId: Int = 0,
    val continentId: Int = 0
)

data class Location(
    val id: Int = 0,
    val name: String = ""
)

data class Department(
    val id: Int = 0,
    val name: String = ""
)

data class Schedule(
    val startTime: String = "",
    val endTime: String = "",
    val days: List<String> = listOf()
)

data class Employee(
    val id: Int = 0,
    val name: String = "",
    val position: String = "",
    val departmentId: Int = 0,
    val email: String = "",
    val phone: String = "",
    val office: Office = Office(),
    val schedule: Schedule = Schedule()
)

// Composable principal
@Composable
fun SearchEmployeeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var filteredEmployees by remember { mutableStateOf(emptyList<Employee>()) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var showEmployeeDetails by remember { mutableStateOf(false) }
    var selectedCountryId by remember { mutableStateOf(-1) }
    var selectedCityId by remember { mutableStateOf(-1) }
    var selectedContinentId by remember { mutableStateOf(-1) }
    var selectedDepartmentId by remember { mutableStateOf(-1) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val employees = remember { mutableStateListOf<Employee>() }
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
        getEmployeesData(employees, searchQuery, selectedContinentId, selectedCountryId, selectedCityId, selectedDepartmentId) {
            filteredEmployees = it
        }
    }

    LaunchedEffect(searchQuery, selectedContinentId, selectedCountryId, selectedCityId, selectedDepartmentId) {
        hasSearched = searchQuery.isNotEmpty()
        getEmployeesData(employees, searchQuery, selectedContinentId, selectedCountryId, selectedCityId, selectedDepartmentId) { result ->
            filteredEmployees = result.sortedBy { it.name }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search employee by name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(onClick = { showFilterDialog = true }) {
                Icon(imageVector = Icons.Default.List, contentDescription = "Filtrar")
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                selectedCountryId = selectedCountryId,
                selectedCityId = selectedCityId,
                selectedContinentId = selectedContinentId,
                onDismiss = { showFilterDialog = false },
                onFilterSelected = { continentId, countryId, cityId ->
                    selectedContinentId = continentId
                    selectedCountryId = countryId
                    selectedCityId = cityId
                    showFilterDialog = false
                    getEmployeesData(employees, searchQuery, selectedContinentId, selectedCountryId, selectedCityId, selectedDepartmentId) { result ->
                        filteredEmployees = result
                    }
                },
                resetFilters = {
                    selectedCountryId = -1
                    selectedCityId = -1
                    selectedContinentId = -1
                    getEmployeesData(employees, searchQuery, selectedContinentId, selectedCountryId, selectedCityId, selectedDepartmentId) { result ->
                        filteredEmployees = result
                    }
                },
                availableCountries = countries,
                availableCities = cities,
                availableContinents = continents,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (hasSearched) {
            if (filteredEmployees.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredEmployees) { employee ->
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


@Composable
fun EmployeeDetailsDialog(
    employee: Employee,
    availableCountries: List<Country>,
    availableCities: List<City>,
    availableContinents: List<Continent>,
    availableLocations: List<Location>,
    availableDepartments: List<Department>,
    onDismiss: () -> Unit
) {
    val countryName = availableCountries.find { it.id == employee.office.countryId }?.name ?: "No disponible"
    val cityName = availableCities.find { it.id == employee.office.cityId }?.name ?: "No disponible"
    val continentName = availableContinents.find { it.id == employee.office.continentId }?.name ?: "No disponible"
    val locationName = availableLocations.find { it.id == employee.office.location }?.name ?: "No disponible"
    val departmentName = availableDepartments.find { it.id == employee.departmentId }?.name ?: "No disponible"
    println("Employee Department ID: ${employee.departmentId}, Name: $departmentName")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(employee.name) },
        text = {
            Column {
                Text("Position: ${employee.position}")
                Text("Department: $departmentName")
                Text("Mail: ${employee.email}")
                Text("Phone: ${employee.phone}")
                Text("Office: $locationName")
                Text("Continent: $continentName")
                Text("Country: $countryName")
                Text("City: $cityName")
                Text("Schedule: ${employee.schedule.startTime} - ${employee.schedule.endTime}")
                Text("Days: ${employee.schedule.days.joinToString(", ")}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


private fun getEmployeesData(
    employees: SnapshotStateList<Employee>,
    searchQuery: String,
    continentId: Int,
    countryId: Int,
    cityId: Int,
    departmentId: Int,
    onFilteredEmployeesUpdated: (List<Employee>) -> Unit
) {
    val database = Firebase.database
    val employeesRef = database.getReference("Employees")

    employeesRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            employees.clear()
            for (employeeSnapshot in snapshot.children) {
                try {
                    val employee = employeeSnapshot.getValue(Employee::class.java)
                    if (employee != null &&
                        (continentId == -1 || employee.office.continentId == continentId) &&
                        (countryId == -1 || employee.office.countryId == countryId) &&
                        (cityId == -1 || employee.office.cityId == cityId) &&
                        (departmentId == -1 || employee.departmentId == departmentId) &&  // Corrected to employee.departmentId
                        employee.name.lowercase().contains(searchQuery.lowercase())
                    ) {
                        employees.add(employee)
                    }
                } catch (e: Exception) {
                    println("Error deserializing employee: ${e.message}")
                }
            }

            onFilteredEmployeesUpdated(employees.sortedBy { it.name })
            println("Filtered Employees Count: ${employees.size}") // Debugging output
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error reading database: ${error.message}")
        }
    })
}




fun getContinentsData(continents: MutableList<Continent>, onContinentsUpdated: (List<Continent>) -> Unit) {
    val database = Firebase.database
    val continentsRef = database.getReference("continents")

    continentsRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            continents.clear()
            for (continentSnapshot in snapshot.children) {
                val continent = continentSnapshot.getValue(Continent::class.java)
                continent?.let { continents.add(it) }
            }
            continents.sortBy { it.name }
            onContinentsUpdated(continents)
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error al leer la base de datos: ${error.message}")
        }
    })
}

fun getCountriesData(countries: MutableList<Country>, onCountriesUpdated: (List<Country>) -> Unit) {
    val database = Firebase.database
    val countriesRef = database.getReference("countries")

    countriesRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            countries.clear()
            for (countrySnapshot in snapshot.children) {
                val country = countrySnapshot.getValue(Country::class.java)
                country?.let { countries.add(it) }
            }
            countries.sortBy { it.name }
            onCountriesUpdated(countries)
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error al leer la base de datos: ${error.message}")
        }
    })
}

fun getCitiesData(cities: MutableList<City>, onCitiesUpdated: (List<City>) -> Unit) {
    val database = Firebase.database
    val citiesRef = database.getReference("cities")

    citiesRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            cities.clear()
            for (citySnapshot in snapshot.children) {
                val city = citySnapshot.getValue(City::class.java)
                city?.let { cities.add(it) }
            }
            cities.sortBy { it.name }
            onCitiesUpdated(cities)
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error al leer la base de datos: ${error.message}")
        }
    })
}

fun getDepartmentsData(departments: MutableList<Department>, onDepartmentsUpdated: (List<Department>) -> Unit) {
    val database = Firebase.database
    val departmentsRef = database.getReference("departments")

    departmentsRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            departments.clear()
            for (departmentSnapshot in snapshot.children) {
                val department = departmentSnapshot.getValue(Department::class.java)
                department?.let { departments.add(it) }
            }
            departments.sortBy { it.name }
            onDepartmentsUpdated(departments)
            println("Departamentos cargados: ${departments.joinToString { it.name }}") // Agrega esto para ver los departamentos
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error al leer la base de datos: ${error.message}")
        }
    })
}


// Actualización en la función de aplicar filtros utilizando IDs
private fun applyFilters(
    query: String,
    employees: List<Employee>,
    continentId: Int,
    countryId: Int,
    cityId: Int,
    onFilteredResult: (List<String>) -> Unit
) {
    val filteredResult = employees.filter { employee ->
        employee.name.lowercase().contains(query.lowercase()) &&
                (continentId == -1 || employee.office.continentId == continentId) &&
                (countryId == -1 || employee.office.countryId == countryId) &&
                (cityId == -1 || employee.office.cityId == cityId)
    }.map { it.name }  // Extrae solo los nombres para mostrar
    onFilteredResult(filteredResult)
}

@Composable
fun FilterDialog(
    selectedCountryId: Int,
    selectedCityId: Int,
    selectedContinentId: Int,
    onDismiss: () -> Unit,
    onFilterSelected: (Int, Int, Int) -> Unit,
    resetFilters: () -> Unit,
    availableCountries: List<Country>,
    availableCities: List<City>,
    availableContinents: List<Continent>
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar Empleados") },
        text = {
            Column {
                DropdownFilter(
                    label = "Continente",
                    options = availableContinents,
                    selectedOption = availableContinents.find { it.id == selectedContinentId }
                ) { continent ->
                    onFilterSelected(continent.id, selectedCountryId, selectedCityId)
                }
                DropdownFilter(
                    label = "País",
                    options = availableCountries,
                    selectedOption = availableCountries.find { it.id == selectedCountryId }
                ) { country ->
                    onFilterSelected(selectedContinentId, country.id, selectedCityId)
                }
                DropdownFilter(
                    label = "Ciudad",
                    options = availableCities,
                    selectedOption = availableCities.find { it.id == selectedCityId }
                ) { city ->
                    onFilterSelected(selectedContinentId, selectedCountryId, city.id)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onFilterSelected(selectedContinentId, selectedCountryId, selectedCityId)
            }) {
                Text("Aplicar Filtros")
            }
        },
        dismissButton = {
            Button(onClick = {
                resetFilters()
                onDismiss()
            }) {
                Text("Reiniciar Filtros")
            }
        }
    )
}

@Composable
fun <T> DropdownFilter(label: String, options: List<T>, selectedOption: T?, onOptionSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = selectedOption?.let { (it as? Continent)?.name ?: (it as? Country)?.name ?: (it as? City)?.name } ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            readOnly = true
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }, text = { Text((option as? Continent)?.name ?: (option as? Country)?.name ?: (option as? City)?.name ?: "") })
            }
        }
    }
}

