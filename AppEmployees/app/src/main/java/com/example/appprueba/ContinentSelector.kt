import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ContinentSelector(modifier: Modifier = Modifier, onSelectContinent: (String) -> Unit) {

    // Lista de continentes
    val continents = listOf("África", "América", "Asia", "Europa", "Oceanía", "Antártida")

    // Caja principal que ocupa todo el tamaño disponible
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la sección
            Text(text = "Selecciona un continente")

            // Generar botones dinámicamente a partir de la lista de continentes
            continents.forEach { continent ->
                Button(onClick = {onSelectContinent(continent)
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = continent) // Asegúrate de que el texto esté correctamente configurado
                }

            // Espacio entre los botones y la visualización del continente seleccionado
            Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }
}

/*

// Vista previa de ContinentSelector
@Preview(showBackground = true)
@Composable
fun PreviewContinentSelector() {
    ContinentSelector()
}*/
