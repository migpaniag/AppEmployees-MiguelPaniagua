package com.example.appprueba

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*


@Composable
fun SearchGeneral() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Employees", "Offices", "Departments")

    Column {
        // Bar de pestañas
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                )
            }
        }

        // Contenido según la pestaña seleccionada
        when (selectedTabIndex) {
            0 -> SearchEmployeeScreen()
            1 -> SearchLocationScreen()
            2 -> SearchDepartmentScreen()
        }
    }
}