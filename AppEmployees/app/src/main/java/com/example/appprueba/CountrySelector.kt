import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CountrySelector(continent: String, modifier: Modifier = Modifier, onVolver: () -> Unit, onSelectCountry: (String) -> Unit) {
    // Estado para manejar el continente seleccionado
    val selectedContinent = remember { mutableStateOf(continent) }
    val selectedCountry = remember { mutableStateOf("Ninguno") }
    // Lista de continentes
    val countries = listOf("España", "Francia", "Portugal", "Italia", "Alemania", "Grecia")

    // Caja principal que ocupa todo el tamaño disponible
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Selecciona un país")
            Text(text = selectedContinent.value)

            // Generar botones dinámicamente a partir de la lista de continentes
            countries.forEach { country ->
                Button(onClick = {selectedCountry.value = country}, modifier = modifier.fillMaxWidth()) {
                    Text(country)
                }
            }

            // Espacio entre los botones y la visualización del continente seleccionado
            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar el continente seleccionado
            Text(text = "País seleccionado: ${selectedCountry.value}")
            Button(onClick = { onVolver() } , modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}


/*
// Vista previa de ContinentSelector
@Preview(showBackground = true)
@Composable
fun PreviewCountrySelector() {
    ContinentSelector()
}*/
