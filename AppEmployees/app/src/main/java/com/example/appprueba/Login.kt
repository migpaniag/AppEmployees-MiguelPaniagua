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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun Login(modifier: Modifier = Modifier, onLoginSuccess: () -> Unit) {
    // Estado para manejar el texto de los campos
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Caja principal que ocupa todo el tamaño disponible
    Box(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Login")
            Spacer(modifier = Modifier.height(8.dp))
            // Campo de texto para el nombre de usuario
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            // Espacio entre los campos
            Spacer(modifier = Modifier.height(8.dp))

            // Campo de texto para la contraseña
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            // Espacio entre los campos y el botón
            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de error si es necesario
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = androidx.compose.ui.graphics.Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botón de enviar
            Button(onClick = { validateLogin(username, password, onLoginSuccess, { errorMessage = it }) }) {
                Text("Enviar")
            }
        }
    }
}

// Función para validar el login
private fun validateLogin(username: String, password: String, onLoginSuccess: () -> Unit, onError: (String) -> Unit) {
    val database = Firebase.database
    val usersRef = database.getReference("users")

    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var userFound = false
            for (userSnapshot in snapshot.children) {
                val user = userSnapshot.getValue(User::class.java)
                if (user != null && user.user == username && user.password == password) {
                    userFound = true
                    break
                }
            }
            if (userFound) {
                onLoginSuccess() // Si las credenciales son correctas, llama a onLoginSuccess
            } else {
                onError("Usuario o contraseña incorrectos") // Si no se encuentra al usuario
            }
        }

        override fun onCancelled(error: DatabaseError) {
            onError("Error al acceder a la base de datos: ${error.message}")
        }
    })
}

// Clase de datos para el usuario
data class User(val user: String = "", val password: String = "")
